package zed.rainxch.githubstore.feature.details.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.mikepenz.markdown.coil3.Coil3ImageTransformerImpl
import com.mikepenz.markdown.compose.Markdown
import githubstore.composeapp.generated.resources.Res
import githubstore.composeapp.generated.resources.ic_github
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import zed.rainxch.githubstore.core.domain.model.GithubRelease
import zed.rainxch.githubstore.core.domain.model.GithubUserProfile
import zed.rainxch.githubstore.core.presentation.components.GithubStoreButton
import zed.rainxch.githubstore.core.presentation.theme.GithubStoreTheme
import zed.rainxch.githubstore.feature.details.domain.model.RepoStats
import zed.rainxch.githubstore.feature.details.presentation.components.AppHeader
import zed.rainxch.githubstore.feature.details.presentation.components.SmartInstallButton
import zed.rainxch.githubstore.feature.details.presentation.utils.rememberMarkdownColors
import zed.rainxch.githubstore.feature.details.presentation.utils.rememberMarkdownTypography

@Composable
fun DetailsRoot(
    onNavigateBack: () -> Unit,
    viewModel: DetailsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    DetailsScreen(
        state = state,
        onAction = { action ->
            when (action) {
                DetailsAction.OnNavigateBackClick -> {
                    onNavigateBack()
                }

                is DetailsAction.OpenAuthorInApp -> {

                }

                else -> {
                    viewModel.onAction(action)
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    state: DetailsState,
    onAction: (DetailsAction) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            onAction(DetailsAction.OnNavigateBackClick)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate Back",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                actions = {
                    state.repository?.htmlUrl?.let {
                        IconButton(
                            onClick = {
                                onAction(DetailsAction.OpenRepoInBrowser)
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInBrowser,
                                contentDescription = "Open repository",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            return@Scaffold
        }

        if (state.errorMessage != null) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Error loading details",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = state.errorMessage,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.error,
                )

                GithubStoreButton(
                    text = "Retry",
                    onClick = {
                        onAction(DetailsAction.Retry)
                    }
                )
            }

            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                if (state.repository != null) {
                    AppHeader(
                        author = state.userProfile,
                        release = state.latestRelease,
                        repository = state.repository
                    )
                }
            }

            item {
                SmartInstallButton(
                    isDownloading = state.isDownloading,
                    isInstalling = state.isInstalling,
                    progress = state.downloadProgressPercent,
                    primaryAsset = state.primaryAsset,
                    state = state,
                    onClick = { onAction(DetailsAction.InstallPrimary) }
                )

            }

            state.stats?.let { stats ->
                Stats(repoStats = stats)
            }

            if (!state.readmeMarkdown.isNullOrBlank()) {
                About(state.readmeMarkdown)
            }

            if (state.latestRelease != null) {
                WhatsNew(state.latestRelease)
            }

            if (state.repository?.owner != null) {
                Owner(
                    author = state.userProfile,
                    onAction = onAction
                )
            }

            if (state.installLogs.isNotEmpty()) {
                Logs(state)
            }
        }
    }
}


private fun LazyListScope.Stats(
    repoStats: RepoStats,
) {
    item {
        Spacer(Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatItem(
                label = "Forks",
                stat = repoStats.forks,
                modifier = Modifier.weight(1.5f)
            )

            StatItem(
                label = "Stars",
                stat = repoStats.stars,
                modifier = Modifier.weight(2f)
            )

            StatItem(
                label = "Issues",
                stat = repoStats.openIssues,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    stat: Int,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier,
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.outline
            )

            Text(
                text = stat.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

private fun LazyListScope.Owner(
    author: GithubUserProfile?,
    onAction: (DetailsAction) -> Unit
) {
    item {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Author",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 12.dp),
            fontWeight = FontWeight.Bold,
        )

        OutlinedCard(
            onClick = {
                author?.id?.toInt()?.let { authorId ->
                    onAction(DetailsAction.OpenAuthorInApp(authorId))
                }
            },
            colors = CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
            ),
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = author?.avatarUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    author?.login?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }

                    author?.bio?.let { bio ->
                        Text(
                            text = bio,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline,
                            maxLines = 2,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    author?.htmlUrl?.let {
                        Row(
                            modifier = Modifier.clickable {
                                onAction(DetailsAction.OpenAuthorInBrowser)
                            },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_github),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )

                            Text(
                                text = "Profile",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }

                IconButton(
                    onClick = {
                        author?.id?.toInt()?.let { authorId ->
                            onAction(DetailsAction.OpenAuthorInApp(authorId))
                        }
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Navigate Back",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

private fun LazyListScope.WhatsNew(latestRelease: GithubRelease) {
    item {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Spacer(Modifier.height(16.dp))

        Text(
            text = "What's New",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp),
            fontWeight = FontWeight.Bold,
        )

        Spacer(Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        latestRelease.tagName,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        latestRelease.publishedAt.take(10),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(12.dp))

                val colors = rememberMarkdownColors()
                val typography = rememberMarkdownTypography()
                val flavour = remember { GFMFlavourDescriptor() }

                Markdown(
                    content = latestRelease.description ?: "No release notes.",
                    colors = colors,
                    typography = typography,
                    flavour = flavour,
                    imageTransformer = Coil3ImageTransformerImpl,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

private fun LazyListScope.About(readmeMarkdown: String) {
    item {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Spacer(Modifier.height(16.dp))

        Text(
            text = "About this app",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }

    item {
        Surface(
            color = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground
        ) {
            val colors = rememberMarkdownColors()
            val typography = rememberMarkdownTypography()
            val flavour = remember { GFMFlavourDescriptor() }

            Markdown(
                content = readmeMarkdown,
                colors = colors,
                typography = typography,
                flavour = flavour,
                imageTransformer = Coil3ImageTransformerImpl,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private fun LazyListScope.Logs(state: DetailsState) {
    item {
        HorizontalDivider()

        Text(
            text = "Install logs",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(vertical = 8.dp),
            fontWeight = FontWeight.Bold,
        )
    }

    items(state.installLogs) { log ->
        Text(
            text = "> ${log.result}: ${log.assetName}",
            style = MaterialTheme.typography.labelSmall.copy(
                fontStyle = FontStyle.Italic
            ),
            color = if (log.result.startsWith("Error")) {
                MaterialTheme.colorScheme.error
            } else MaterialTheme.colorScheme.outline
        )
    }
}

@Preview
@Composable
private fun Preview() {
    GithubStoreTheme {
        DetailsScreen(
            state = DetailsState(),
            onAction = {}
        )
    }
}