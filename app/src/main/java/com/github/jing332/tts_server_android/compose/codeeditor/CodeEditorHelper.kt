package com.github.jing332.text_searcher.ui.plugin

import android.content.Context
import android.content.res.Configuration
import com.github.jing332.tts_server_android.constant.CodeEditorTheme
import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry
import io.github.rosemoe.sora.langs.textmate.registry.dsl.languages
import io.github.rosemoe.sora.langs.textmate.registry.model.ThemeModel
import io.github.rosemoe.sora.langs.textmate.registry.provider.AssetsFileResolver
import io.github.rosemoe.sora.widget.CodeEditor
import org.eclipse.tm4e.core.registry.IThemeSource


class CodeEditorHelper(val context: Context, val editor: CodeEditor) {
    fun initEditor() {
        FileProviderRegistry.getInstance().addFileProvider(AssetsFileResolver(context.assets))

        val themes = arrayOf(
            "textmate/quietlight.json",
            "textmate/solarized_drak.json",
            "textmate/darcula.json",
            "textmate/abyss.json"
        )
        val themeRegistry = ThemeRegistry.getInstance()
        for (theme in themes) {
            themeRegistry.loadTheme(
                ThemeModel(
                    IThemeSource.fromInputStream(
                        FileProviderRegistry.getInstance().tryGetInputStream(theme), theme, null
                    )
                )
            )
        }

        GrammarRegistry.getInstance().loadGrammars(languages {
            language("js") {
                grammar = "textmate/javascript/syntaxes/JavaScript.tmLanguage.json"
                defaultScopeName()
                languageConfiguration = "textmate/javascript/language-configuration.json"
            }
        })
        editor.setEditorLanguage(TextMateLanguage.create("source.js", true))

    }

    fun setTheme(theme: CodeEditorTheme) {
        val themeRegistry = ThemeRegistry.getInstance()
        when (theme) {
            CodeEditorTheme.AUTO -> {
                val isNight =
                    (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
                setTheme(if (isNight) CodeEditorTheme.DARCULA else CodeEditorTheme.QUIET_LIGHT)
                return
            }

            else-> {
                themeRegistry.setTheme(theme.id)
                ensureTextmateTheme()
                return
            }
        }
    }

    private fun ensureTextmateTheme() {
        var editorColorScheme = editor.colorScheme
        if (editorColorScheme !is TextMateColorScheme) {
            editorColorScheme = TextMateColorScheme.create(ThemeRegistry.getInstance())
            editor.colorScheme = editorColorScheme
        }
    }

}