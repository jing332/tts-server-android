package com.github.jing332.tts_server_android.model.updater


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WorkflowRuns(
    @SerialName("total_count")
    val totalCount: Int = 0, // 468
    @SerialName("workflow_runs")
    val workflowRuns: List<WorkflowRun> = listOf()
) {
    @Serializable
    data class WorkflowRun(
//        @SerialName("actor")
        val actor: Actor = Actor(),
//        @SerialName("artifacts_url")
//        val artifactsUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/actions/runs/7605412793/artifacts
//        @SerialName("cancel_url")
//        val cancelUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/actions/runs/7605412793/cancel
//        @SerialName("check_suite_id")
//        val checkSuiteId: Long = 0, // 19988906797
//        @SerialName("check_suite_node_id")
//        val checkSuiteNodeId: String = "", // CS_kwDOH_7t188AAAAEp26DLQ
//        @SerialName("check_suite_url")
//        val checkSuiteUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/check-suites/19988906797
        @SerialName("conclusion")
        val conclusion: String? = "", // success
        @SerialName("created_at")
        val createdAt: String = "", // 2024-01-22T01:49:46Z
        @SerialName("display_title")
        val displayTitle: String = "", // refactor: 插件UI 滑动条
//        @SerialName("event")
//        val event: String = "", // push
//        @SerialName("head_branch")
//        val headBranch: String = "", // compose
//        @SerialName("head_commit")
//        val headCommit: HeadCommit = HeadCommit(),
//        @SerialName("head_repository")
//        val headRepository: HeadRepository = HeadRepository(),
//        @SerialName("head_sha")
//        val headSha: String = "", // 3f23a7603a75b9218e61554fd5ec69fe6377abc4
        @SerialName("html_url")
        val htmlUrl: String = "", // https://github.com/jing332/tts-server-android/actions/runs/7605412793
//        @SerialName("id")
//        val id: Long = 0, // 7605412793
//        @SerialName("jobs_url")
//        val jobsUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/actions/runs/7605412793/jobs
//        @SerialName("logs_url")
//        val logsUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/actions/runs/7605412793/logs
//        @SerialName("name")
//        val name: String = "", // Build Test
//        @SerialName("node_id")
//        val nodeId: String = "", // WFR_kwLOH_7t188AAAABxVFjuQ
        @SerialName("path")
        val path: String = "", // .github/workflows/test.yml
//        @SerialName("previous_attempt_url")
//        val previousAttemptUrl: String? = "", // https://api.github.com/repos/jing332/tts-server-android/actions/runs/7583634607/attempts/1
//        @SerialName("pull_requests")
//        val pullRequests: List<Any> = listOf(),
//        @SerialName("referenced_workflows")
//        val referencedWorkflows: List<Any> = listOf(),
//        @SerialName("repository")
//        val repository: Repository = Repository(),
//        @SerialName("rerun_url")
//        val rerunUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/actions/runs/7605412793/rerun
//        @SerialName("run_attempt")
//        val runAttempt: Int = 0, // 1
//        @SerialName("run_number")
//        val runNumber: Int = 0, // 603
//        @SerialName("run_started_at")
//        val runStartedAt: String = "", // 2024-01-22T01:49:46Z
        @SerialName("status")
        val status: String = "", // in_progress
//        @SerialName("triggering_actor")
//        val triggeringActor: TriggeringActor = TriggeringActor(),
        @SerialName("updated_at")
        val updatedAt: String = "", // 2024-01-22T01:49:54Z
//        @SerialName("url")
//        val url: String = "", // https://api.github.com/repos/jing332/tts-server-android/actions/runs/7605412793
//        @SerialName("workflow_id")
//        val workflowId: Int = 0, // 35098665
//        @SerialName("workflow_url")
//        val workflowUrl: String = "" // https://api.github.com/repos/jing332/tts-server-android/actions/workflows/35098665
    ) {
        @Serializable
        data class Actor(
            @SerialName("avatar_url")
            val avatarUrl: String = "", // https://avatars.githubusercontent.com/u/42014615?v=4
            @SerialName("events_url")
            val eventsUrl: String = "", // https://api.github.com/users/jing332/events{/privacy}
            @SerialName("followers_url")
            val followersUrl: String = "", // https://api.github.com/users/jing332/followers
            @SerialName("following_url")
            val followingUrl: String = "", // https://api.github.com/users/jing332/following{/other_user}
            @SerialName("gists_url")
            val gistsUrl: String = "", // https://api.github.com/users/jing332/gists{/gist_id}
            @SerialName("gravatar_id")
            val gravatarId: String = "",
            @SerialName("html_url")
            val htmlUrl: String = "", // https://github.com/jing332
            @SerialName("id")
            val id: Int = 0, // 42014615
            @SerialName("login")
            val login: String = "", // jing332
            @SerialName("node_id")
            val nodeId: String = "", // MDQ6VXNlcjQyMDE0NjE1
            @SerialName("organizations_url")
            val organizationsUrl: String = "", // https://api.github.com/users/jing332/orgs
            @SerialName("received_events_url")
            val receivedEventsUrl: String = "", // https://api.github.com/users/jing332/received_events
            @SerialName("repos_url")
            val reposUrl: String = "", // https://api.github.com/users/jing332/repos
            @SerialName("site_admin")
            val siteAdmin: Boolean = false, // false
            @SerialName("starred_url")
            val starredUrl: String = "", // https://api.github.com/users/jing332/starred{/owner}{/repo}
            @SerialName("subscriptions_url")
            val subscriptionsUrl: String = "", // https://api.github.com/users/jing332/subscriptions
            @SerialName("type")
            val type: String = "", // User
            @SerialName("url")
            val url: String = "" // https://api.github.com/users/jing332
        )

        /* @Serializable
         data class HeadCommit(
             @SerialName("author")
             val author: Author = Author(),
             @SerialName("committer")
             val committer: Committer = Committer(),
             @SerialName("id")
             val id: String = "", // 3f23a7603a75b9218e61554fd5ec69fe6377abc4
             @SerialName("message")
             val message: String = "", // refactor: 插件UI 滑动条
             @SerialName("timestamp")
             val timestamp: String = "", // 2024-01-22T01:49:33Z
             @SerialName("tree_id")
             val treeId: String = "" // 9029bfee62475a1a775e31862dce31cbe571da5d
         ) {
             @Serializable
             data class Author(
                 @SerialName("email")
                 val email: String = "", // 42014615+jing332@users.noreply.github.com
                 @SerialName("name")
                 val name: String = "" // Jing
             )

             @Serializable
             data class Committer(
                 @SerialName("email")
                 val email: String = "", // 42014615+jing332@users.noreply.github.com
                 @SerialName("name")
                 val name: String = "" // Jing
             )
         }

         @Serializable
         data class HeadRepository(
             @SerialName("archive_url")
             val archiveUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/{archive_format}{/ref}
             @SerialName("assignees_url")
             val assigneesUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/assignees{/user}
             @SerialName("blobs_url")
             val blobsUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/git/blobs{/sha}
             @SerialName("branches_url")
             val branchesUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/branches{/branch}
             @SerialName("collaborators_url")
             val collaboratorsUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/collaborators{/collaborator}
             @SerialName("comments_url")
             val commentsUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/comments{/number}
             @SerialName("commits_url")
             val commitsUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/commits{/sha}
             @SerialName("compare_url")
             val compareUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/compare/{base}...{head}
             @SerialName("contents_url")
             val contentsUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/contents/{+path}
             @SerialName("contributors_url")
             val contributorsUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/contributors
             @SerialName("deployments_url")
             val deploymentsUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/deployments
             @SerialName("description")
             val description: String = "", // 这是一个Android系统TTS应用，内置微软演示接口，可自定义HTTP请求，可导入其他本地TTS引擎，以及根据中文双引号的简单旁白/对话识别朗读 ，还有自动重试，备用配置，文本替换等更多功能。
             @SerialName("downloads_url")
             val downloadsUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/downloads
             @SerialName("events_url")
             val eventsUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/events
             @SerialName("fork")
             val fork: Boolean = false, // false
             @SerialName("forks_url")
             val forksUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/forks
             @SerialName("full_name")
             val fullName: String = "", // jing332/tts-server-android
             @SerialName("git_commits_url")
             val gitCommitsUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/git/commits{/sha}
             @SerialName("git_refs_url")
             val gitRefsUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/git/refs{/sha}
             @SerialName("git_tags_url")
             val gitTagsUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/git/tags{/sha}
             @SerialName("hooks_url")
             val hooksUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/hooks
             @SerialName("html_url")
             val htmlUrl: String = "", // https://github.com/jing332/tts-server-android
             @SerialName("id")
             val id: Int = 0, // 536800727
             @SerialName("issue_comment_url")
             val issueCommentUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/issues/comments{/number}
             @SerialName("issue_events_url")
             val issueEventsUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/issues/events{/number}
             @SerialName("issues_url")
             val issuesUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/issues{/number}
             @SerialName("keys_url")
             val keysUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/keys{/key_id}
             @SerialName("labels_url")
             val labelsUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/labels{/name}
             @SerialName("languages_url")
             val languagesUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/languages
             @SerialName("merges_url")
             val mergesUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/merges
             @SerialName("milestones_url")
             val milestonesUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/milestones{/number}
             @SerialName("name")
             val name: String = "", // tts-server-android
             @SerialName("node_id")
             val nodeId: String = "", // R_kgDOH_7t1w
             @SerialName("notifications_url")
             val notificationsUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/notifications{?since,all,participating}
             @SerialName("owner")
             val owner: Owner = Owner(),
             @SerialName("private")
             val `private`: Boolean = false, // false
             @SerialName("pulls_url")
             val pullsUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/pulls{/number}
             @SerialName("releases_url")
             val releasesUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/releases{/id}
             @SerialName("stargazers_url")
             val stargazersUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/stargazers
             @SerialName("statuses_url")
             val statusesUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/statuses/{sha}
             @SerialName("subscribers_url")
             val subscribersUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/subscribers
             @SerialName("subscription_url")
             val subscriptionUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/subscription
             @SerialName("tags_url")
             val tagsUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/tags
             @SerialName("teams_url")
             val teamsUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/teams
             @SerialName("trees_url")
             val treesUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/git/trees{/sha}
             @SerialName("url")
             val url: String = "" // https://api.github.com/repos/jing332/tts-server-android
         ) {
             @Serializable
             data class Owner(
                 @SerialName("avatar_url")
                 val avatarUrl: String = "", // https://avatars.githubusercontent.com/u/42014615?v=4
                 @SerialName("events_url")
                 val eventsUrl: String = "", // https://api.github.com/users/jing332/events{/privacy}
                 @SerialName("followers_url")
                 val followersUrl: String = "", // https://api.github.com/users/jing332/followers
                 @SerialName("following_url")
                 val followingUrl: String = "", // https://api.github.com/users/jing332/following{/other_user}
                 @SerialName("gists_url")
                 val gistsUrl: String = "", // https://api.github.com/users/jing332/gists{/gist_id}
                 @SerialName("gravatar_id")
                 val gravatarId: String = "",
                 @SerialName("html_url")
                 val htmlUrl: String = "", // https://github.com/jing332
                 @SerialName("id")
                 val id: Int = 0, // 42014615
                 @SerialName("login")
                 val login: String = "", // jing332
                 @SerialName("node_id")
                 val nodeId: String = "", // MDQ6VXNlcjQyMDE0NjE1
                 @SerialName("organizations_url")
                 val organizationsUrl: String = "", // https://api.github.com/users/jing332/orgs
                 @SerialName("received_events_url")
                 val receivedEventsUrl: String = "", // https://api.github.com/users/jing332/received_events
                 @SerialName("repos_url")
                 val reposUrl: String = "", // https://api.github.com/users/jing332/repos
                 @SerialName("site_admin")
                 val siteAdmin: Boolean = false, // false
                 @SerialName("starred_url")
                 val starredUrl: String = "", // https://api.github.com/users/jing332/starred{/owner}{/repo}
                 @SerialName("subscriptions_url")
                 val subscriptionsUrl: String = "", // https://api.github.com/users/jing332/subscriptions
                 @SerialName("type")
                 val type: String = "", // User
                 @SerialName("url")
                 val url: String = "" // https://api.github.com/users/jing332
             )
         }

         @Serializable
         data class Repository(
             @SerialName("archive_url")
             val archiveUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/{archive_format}{/ref}
             @SerialName("assignees_url")
             val assigneesUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/assignees{/user}
             @SerialName("blobs_url")
             val blobsUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/git/blobs{/sha}
             @SerialName("branches_url")
             val branchesUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/branches{/branch}
             @SerialName("collaborators_url")
             val collaboratorsUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/collaborators{/collaborator}
             @SerialName("comments_url")
             val commentsUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/comments{/number}
             @SerialName("commits_url")
             val commitsUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/commits{/sha}
             @SerialName("compare_url")
             val compareUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/compare/{base}...{head}
             @SerialName("contents_url")
             val contentsUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/contents/{+path}
             @SerialName("contributors_url")
             val contributorsUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/contributors
             @SerialName("deployments_url")
             val deploymentsUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/deployments
             @SerialName("description")
             val description: String = "", // 这是一个Android系统TTS应用，内置微软演示接口，可自定义HTTP请求，可导入其他本地TTS引擎，以及根据中文双引号的简单旁白/对话识别朗读 ，还有自动重试，备用配置，文本替换等更多功能。
             @SerialName("downloads_url")
             val downloadsUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/downloads
             @SerialName("events_url")
             val eventsUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/events
             @SerialName("fork")
             val fork: Boolean = false, // false
             @SerialName("forks_url")
             val forksUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/forks
             @SerialName("full_name")
             val fullName: String = "", // jing332/tts-server-android
             @SerialName("git_commits_url")
             val gitCommitsUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/git/commits{/sha}
             @SerialName("git_refs_url")
             val gitRefsUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/git/refs{/sha}
             @SerialName("git_tags_url")
             val gitTagsUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/git/tags{/sha}
             @SerialName("hooks_url")
             val hooksUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/hooks
             @SerialName("html_url")
             val htmlUrl: String = "", // https://github.com/jing332/tts-server-android
             @SerialName("id")
             val id: Int = 0, // 536800727
             @SerialName("issue_comment_url")
             val issueCommentUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/issues/comments{/number}
             @SerialName("issue_events_url")
             val issueEventsUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/issues/events{/number}
             @SerialName("issues_url")
             val issuesUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/issues{/number}
             @SerialName("keys_url")
             val keysUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/keys{/key_id}
             @SerialName("labels_url")
             val labelsUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/labels{/name}
             @SerialName("languages_url")
             val languagesUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/languages
             @SerialName("merges_url")
             val mergesUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/merges
             @SerialName("milestones_url")
             val milestonesUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/milestones{/number}
             @SerialName("name")
             val name: String = "", // tts-server-android
             @SerialName("node_id")
             val nodeId: String = "", // R_kgDOH_7t1w
             @SerialName("notifications_url")
             val notificationsUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/notifications{?since,all,participating}
             @SerialName("owner")
             val owner: Owner = Owner(),
             @SerialName("private")
             val `private`: Boolean = false, // false
             @SerialName("pulls_url")
             val pullsUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/pulls{/number}
             @SerialName("releases_url")
             val releasesUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/releases{/id}
             @SerialName("stargazers_url")
             val stargazersUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/stargazers
             @SerialName("statuses_url")
             val statusesUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/statuses/{sha}
             @SerialName("subscribers_url")
             val subscribersUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/subscribers
             @SerialName("subscription_url")
             val subscriptionUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/subscription
             @SerialName("tags_url")
             val tagsUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/tags
             @SerialName("teams_url")
             val teamsUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/teams
             @SerialName("trees_url")
             val treesUrl: String = "", // https://api.github.com/repos/jing332/tts-server-android/git/trees{/sha}
             @SerialName("url")
             val url: String = "" // https://api.github.com/repos/jing332/tts-server-android
         ) {
             @Serializable
             data class Owner(
                 @SerialName("avatar_url")
                 val avatarUrl: String = "", // https://avatars.githubusercontent.com/u/42014615?v=4
                 @SerialName("events_url")
                 val eventsUrl: String = "", // https://api.github.com/users/jing332/events{/privacy}
                 @SerialName("followers_url")
                 val followersUrl: String = "", // https://api.github.com/users/jing332/followers
                 @SerialName("following_url")
                 val followingUrl: String = "", // https://api.github.com/users/jing332/following{/other_user}
                 @SerialName("gists_url")
                 val gistsUrl: String = "", // https://api.github.com/users/jing332/gists{/gist_id}
                 @SerialName("gravatar_id")
                 val gravatarId: String = "",
                 @SerialName("html_url")
                 val htmlUrl: String = "", // https://github.com/jing332
                 @SerialName("id")
                 val id: Int = 0, // 42014615
                 @SerialName("login")
                 val login: String = "", // jing332
                 @SerialName("node_id")
                 val nodeId: String = "", // MDQ6VXNlcjQyMDE0NjE1
                 @SerialName("organizations_url")
                 val organizationsUrl: String = "", // https://api.github.com/users/jing332/orgs
                 @SerialName("received_events_url")
                 val receivedEventsUrl: String = "", // https://api.github.com/users/jing332/received_events
                 @SerialName("repos_url")
                 val reposUrl: String = "", // https://api.github.com/users/jing332/repos
                 @SerialName("site_admin")
                 val siteAdmin: Boolean = false, // false
                 @SerialName("starred_url")
                 val starredUrl: String = "", // https://api.github.com/users/jing332/starred{/owner}{/repo}
                 @SerialName("subscriptions_url")
                 val subscriptionsUrl: String = "", // https://api.github.com/users/jing332/subscriptions
                 @SerialName("type")
                 val type: String = "", // User
                 @SerialName("url")
                 val url: String = "" // https://api.github.com/users/jing332
             )
         }

         @Serializable
         data class TriggeringActor(
             @SerialName("avatar_url")
             val avatarUrl: String = "", // https://avatars.githubusercontent.com/u/42014615?v=4
             @SerialName("events_url")
             val eventsUrl: String = "", // https://api.github.com/users/jing332/events{/privacy}
             @SerialName("followers_url")
             val followersUrl: String = "", // https://api.github.com/users/jing332/followers
             @SerialName("following_url")
             val followingUrl: String = "", // https://api.github.com/users/jing332/following{/other_user}
             @SerialName("gists_url")
             val gistsUrl: String = "", // https://api.github.com/users/jing332/gists{/gist_id}
             @SerialName("gravatar_id")
             val gravatarId: String = "",
             @SerialName("html_url")
             val htmlUrl: String = "", // https://github.com/jing332
             @SerialName("id")
             val id: Int = 0, // 42014615
             @SerialName("login")
             val login: String = "", // jing332
             @SerialName("node_id")
             val nodeId: String = "", // MDQ6VXNlcjQyMDE0NjE1
             @SerialName("organizations_url")
             val organizationsUrl: String = "", // https://api.github.com/users/jing332/orgs
             @SerialName("received_events_url")
             val receivedEventsUrl: String = "", // https://api.github.com/users/jing332/received_events
             @SerialName("repos_url")
             val reposUrl: String = "", // https://api.github.com/users/jing332/repos
             @SerialName("site_admin")
             val siteAdmin: Boolean = false, // false
             @SerialName("starred_url")
             val starredUrl: String = "", // https://api.github.com/users/jing332/starred{/owner}{/repo}
             @SerialName("subscriptions_url")
             val subscriptionsUrl: String = "", // https://api.github.com/users/jing332/subscriptions
             @SerialName("type")
             val type: String = "", // User
             @SerialName("url")
             val url: String = "" // https://api.github.com/users/jing332
         )
    */
    }
}