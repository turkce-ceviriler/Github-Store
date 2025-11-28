package zed.rainxch.githubstore.feature.details.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.HttpHeaders
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import zed.rainxch.githubstore.core.domain.model.GithubAsset
import zed.rainxch.githubstore.core.domain.model.GithubRelease
import zed.rainxch.githubstore.core.domain.model.GithubRepoSummary
import zed.rainxch.githubstore.core.domain.model.GithubUser
import zed.rainxch.githubstore.core.domain.model.GithubUserProfile
import zed.rainxch.githubstore.feature.details.domain.model.RepoStats
import zed.rainxch.githubstore.feature.details.domain.repository.DetailsRepository

class DetailsRepositoryImpl(
    private val github: HttpClient
) : DetailsRepository {

    override suspend fun getRepositoryById(id: Long): GithubRepoSummary {
        val repo: RepoByIdNetwork = github.get("/repositories/$id") {
            header(HttpHeaders.Accept, "application/vnd.github+json")
        }.body()

        return GithubRepoSummary(
            id = repo.id,
            name = repo.name,
            fullName = repo.fullName,
            owner = GithubUser(
                id = repo.owner.id,
                login = repo.owner.login,
                avatarUrl = repo.owner.avatarUrl,
                htmlUrl = repo.owner.htmlUrl
            ),
            description = repo.description,
            htmlUrl = repo.htmlUrl,
            stargazersCount = repo.stars,
            forksCount = repo.forks,
            language = repo.language,
            topics = repo.topics,
            releasesUrl = "https://api.github.com/repos/${repo.owner.login}/${repo.name}/releases{/id}",
            updatedAt = repo.updatedAt
        )
    }

    override suspend fun getLatestPublishedRelease(owner: String, repo: String): GithubRelease? {
        val releases: List<ReleaseNetwork> = github.get("/repos/$owner/$repo/releases") {
            header(HttpHeaders.Accept, "application/vnd.github+json")
            parameter("per_page", 10)
        }.body()

        val latest = releases
            .asSequence()
            .filter { (it.draft != true) && (it.prerelease != true) }
            .sortedByDescending { it.publishedAt ?: it.createdAt ?: "" }
            .firstOrNull()
            ?: return null

        return latest.toDomain(owner, repo)
    }

    override suspend fun getReadme(owner: String, repo: String): String? {
        return try {
            val rawMarkdown = github.get("https://raw.githubusercontent.com/$owner/$repo/master/README.md").body<String>()
            preprocessMarkdown(rawMarkdown, "https://raw.githubusercontent.com/$owner/$repo/master/")
        } catch (t: Throwable) {
            try {
                val rawMarkdown = github.get("https://raw.githubusercontent.com/$owner/$repo/main/README.md").body<String>()
                preprocessMarkdown(rawMarkdown, "https://raw.githubusercontent.com/$owner/$repo/main/")
            } catch (e: Throwable) {
                null
            }
        }
    }

    override suspend fun getRepoStats(owner: String, repo: String): RepoStats {
        val info: RepoInfoNetwork = github.get("/repos/$owner/$repo") {
            header(HttpHeaders.Accept, "application/vnd.github+json")
        }.body()

        return RepoStats(
            stars = info.stars,
            forks = info.forks,
            openIssues = info.openIssues,
        )
    }

    override suspend fun getUserProfile(username: String): GithubUserProfile {
        val user: UserProfileNetwork = github.get("/users/$username") {
            header(HttpHeaders.Accept, "application/vnd.github+json")
        }.body()

        return GithubUserProfile(
            id = user.id,
            login = user.login,
            name = user.name,
            bio = user.bio,
            avatarUrl = user.avatarUrl,
            htmlUrl = user.htmlUrl,
            followers = user.followers,
            following = user.following,
            publicRepos = user.publicRepos,
            location = user.location,
            company = user.company,
            blog = user.blog,
            twitterUsername = user.twitterUsername
        )
    }


    @Serializable
    private data class UserProfileNetwork(
        val id: Long,
        val login: String,
        val name: String? = null,
        val bio: String? = null,
        @SerialName("avatar_url") val avatarUrl: String,
        @SerialName("html_url") val htmlUrl: String,
        val followers: Int,
        val following: Int,
        @SerialName("public_repos") val publicRepos: Int,
        val location: String? = null,
        val company: String? = null,
        val blog: String? = null,
        @SerialName("twitter_username") val twitterUsername: String? = null
    )

    @Serializable
    private data class RepoByIdNetwork(
        val id: Long,
        val name: String,
        @SerialName("full_name") val fullName: String,
        val owner: OwnerNetwork,
        val description: String? = null,
        @SerialName("html_url") val htmlUrl: String,
        @SerialName("stargazers_count") val stars: Int,
        @SerialName("forks_count") val forks: Int,
        val language: String? = null,
        val topics: List<String>? = null,
        @SerialName("updated_at") val updatedAt: String,
    )

    @Serializable
    private data class OwnerNetwork(
        val id: Long,
        val login: String,
        @SerialName("avatar_url") val avatarUrl: String,
        @SerialName("html_url") val htmlUrl: String
    )

    @Serializable
    private data class RepoInfoNetwork(
        @SerialName("stargazers_count") val stars: Int,
        @SerialName("forks_count") val forks: Int,
        @SerialName("open_issues_count") val openIssues: Int,
    )

    @Serializable
    private data class ReleaseNetwork(
        val id: Long,
        @SerialName("tag_name") val tagName: String,
        val name: String? = null,
        val draft: Boolean? = null,
        val prerelease: Boolean? = null,
        val author: OwnerNetwork,
        @SerialName("published_at") val publishedAt: String? = null,
        @SerialName("created_at") val createdAt: String? = null,
        val body: String? = null,
        @SerialName("tarball_url") val tarballUrl: String,
        @SerialName("zipball_url") val zipballUrl: String,
        @SerialName("html_url") val htmlUrl: String,
        val assets: List<AssetNetwork>
    )

    @Serializable
    private data class ContributorStatsNetwork(
        val total: Int? = null,
        val author: OwnerNetwork? = null
    )

    @Serializable
    private data class AssetNetwork(
        val id: Long,
        val name: String,
        @SerialName("content_type") val contentType: String,
        val size: Long,
        @SerialName("browser_download_url") val downloadUrl: String,
        val uploader: OwnerNetwork
    )

    private fun ReleaseNetwork.toDomain(owner: String, repo: String): GithubRelease = GithubRelease(
        id = id,
        tagName = tagName,
        name = name,
        author = GithubUser(
            id = author.id,
            login = author.login,
            avatarUrl = author.avatarUrl,
            htmlUrl = author.htmlUrl
        ),
        publishedAt = publishedAt ?: createdAt ?: "",
        description = body
            ?.replace("<details>", "")
            ?.replace("</details>", "")
            ?.replace("<summary>", "")
            ?.replace("</summary>", "")
            ?.replace("\r\n", "\n")
            ?.let { preprocessMarkdown(it, "https://raw.githubusercontent.com/$owner/$repo/main/") },
        assets = assets.map { it.toDomain() },
        tarballUrl = tarballUrl,
        zipballUrl = zipballUrl,
        htmlUrl = htmlUrl
    )

    private fun preprocessMarkdown(markdown: String, baseUrl: String): String {
        // Regex matches ![alt](relativePath) where relativePath doesn't start with https?://
        return markdown.replace(Regex("!\\[([^\\]]*)\\]\\((?!https?://)([^)]+)\\)")) { match ->
            val alt = match.groupValues[1]
            var relativePath = match.groupValues[2].trim()
            // Clean up common relative prefixes like ./ or /
            if (relativePath.startsWith("./")) relativePath = relativePath.removePrefix("./")
            if (relativePath.startsWith("/")) relativePath = relativePath.removePrefix("/")
            "![$alt]($baseUrl$relativePath)"
        }
    }

    private fun AssetNetwork.toDomain(): GithubAsset = GithubAsset(
        id = id,
        name = name,
        contentType = contentType,
        size = size,
        downloadUrl = downloadUrl,
        uploader = GithubUser(
            id = uploader.id,
            login = uploader.login,
            avatarUrl = uploader.avatarUrl,
            htmlUrl = uploader.htmlUrl
        )
    )
}