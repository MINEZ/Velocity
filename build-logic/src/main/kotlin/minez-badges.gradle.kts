// 构建时生成 README 所用的徽章。
// 仓库是私有的，shields.io 读不到其中的信息，因此直接生成静态 SVG 随仓库提交，免去手动维护。

/**
 * Renders the badges shown at the top of the README into SVG files.
 */
abstract class GenerateBadges : DefaultTask() {

    @get:Input
    abstract val projectVersion: Property<String>

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val protocolVersionSource: RegularFileProperty

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun generate() {
        val version = projectVersion.get()
        // 版本号形如 4.3.0.1：前三位为所基于的上游版本，末位为本 Fork 的修订号。
        val upstreamVersion = FORK_VERSION.matchEntire(version)?.groupValues?.get(1) ?: version

        val directory = outputDirectory.get().asFile
        directory.mkdirs()
        directory.resolve("upstream.svg").writeText(badge("upstream", "Velocity $upstreamVersion", "#1976d2"))
        directory.resolve("version.svg").writeText(badge("version", version, "#ff9800"))
        directory.resolve("minecraft.svg").writeText(badge("Minecraft", minecraftVersions(), "#4caf50"))
    }

    // 从 ProtocolVersion 枚举中取出支持的最低与最高游戏版本。
    private fun minecraftVersions(): String {
        val source = protocolVersionSource.get().asFile.readText()
        val entries = PROTOCOL_ENTRY.findAll(source)
            .map { entry -> QUOTED.findAll(entry.groupValues[1]).map { it.groupValues[1] }.toList() }
            .filter { it.isNotEmpty() }
            .toList()
        check(entries.isNotEmpty()) { "未能从 ProtocolVersion 中解析出任何游戏版本。" }
        return "${entries.first().first()}–${entries.last().last()}"
    }

    // 与 shields.io 的 flat 样式保持一致。
    private fun badge(label: String, message: String, color: String): String {
        val labelWidth = textWidth(label) + 10
        val messageWidth = textWidth(message) + 10
        val width = labelWidth + messageWidth
        val title = "$label: $message"
        return buildString {
            append("""<svg xmlns="http://www.w3.org/2000/svg" width="$width" height="20" role="img" aria-label="$title">""")
            append("<title>$title</title>")
            append("""<linearGradient id="s" x2="0" y2="100%"><stop offset="0" stop-color="#bbb" stop-opacity=".1"/><stop offset="1" stop-opacity=".1"/></linearGradient>""")
            append("""<clipPath id="r"><rect width="$width" height="20" rx="3" fill="#fff"/></clipPath>""")
            append("""<g clip-path="url(#r)"><rect width="$labelWidth" height="20" fill="#555"/><rect x="$labelWidth" width="$messageWidth" height="20" fill="$color"/><rect width="$width" height="20" fill="url(#s)"/></g>""")
            append("""<g fill="#fff" text-anchor="middle" font-family="Verdana,Geneva,DejaVu Sans,sans-serif" text-rendering="geometricPrecision" font-size="110">""")
            append(text(label, labelWidth * 5))
            append(text(message, labelWidth * 10 + messageWidth * 5))
            append("</g></svg>\n")
        }
    }

    private fun text(content: String, x: Int): String {
        val length = textWidth(content) * 10
        return """<text aria-hidden="true" x="$x" y="150" fill="#010101" fill-opacity=".3" transform="scale(.1)" textLength="$length">$content</text>""" +
            """<text x="$x" y="140" transform="scale(.1)" fill="#fff" textLength="$length">$content</text>"""
    }

    // 按 11px Verdana 的字宽估算文字宽度，数据取自 shields.io 所用的 anafanafo。
    private fun textWidth(content: String): Int =
        Math.round(content.sumOf { VERDANA_WIDTHS[it] ?: 7.0 }).toInt()

    private companion object {
        val FORK_VERSION = Regex("""(\d+\.\d+\.\d+)\.\d+""")
        val PROTOCOL_ENTRY = Regex("""MINECRAFT_\w+\(\s*-?\d+\s*,([^)]*)\)""")
        val QUOTED = Regex(""""([^"]+)"""")
        val VERDANA_WIDTHS: Map<Char, Double> = buildMap {
            val widths = listOf(
                3.87, 4.33, 5.05, 9.0, 6.99, 11.84, 7.99, 2.95, 5.0, 5.0, 6.99, 9.0, 4.0, 5.0, 4.0, 5.0,
                6.99, 6.99, 6.99, 6.99, 6.99, 6.99, 6.99, 6.99, 6.99, 6.99, 5.0, 5.0, 9.0, 9.0, 9.0, 6.0,
                11.0, 7.52, 7.54, 7.68, 8.48, 6.96, 6.32, 8.53, 8.27, 4.63, 5.0, 7.62, 6.12, 9.27, 8.23, 8.66,
                6.63, 8.66, 7.65, 7.52, 6.78, 8.05, 7.52, 10.88, 7.54, 6.77, 7.54, 5.0, 5.0, 5.0, 9.0, 6.99,
                6.99, 6.61, 6.85, 5.73, 6.85, 6.55, 3.87, 6.85, 6.96, 3.02, 3.79, 6.51, 3.02, 10.7, 6.96, 6.68,
                6.85, 6.85, 4.69, 5.73, 4.33, 6.96, 6.51, 9.0, 6.51, 6.51, 5.78, 6.98, 5.0, 6.98, 9.0,
            )
            widths.forEachIndexed { index, width -> put(' ' + index, width) }
            put('–', 6.99)
        }
    }
}

val generateBadges = tasks.register<GenerateBadges>("generateBadges") {
    group = "documentation"
    description = "Generates the badges shown in the README."
    projectVersion = project.version.toString()
    protocolVersionSource = layout.projectDirectory.file(
        "api/src/main/java/com/velocitypowered/api/network/ProtocolVersion.java"
    )
    outputDirectory = layout.projectDirectory.dir("docs/badges")
}

plugins.withType<BasePlugin> {
    tasks.named(LifecycleBasePlugin.BUILD_TASK_NAME) {
        dependsOn(generateBadges)
    }
}
