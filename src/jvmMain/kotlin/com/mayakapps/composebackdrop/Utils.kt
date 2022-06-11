package com.mayakapps.composebackdrop

import androidx.compose.ui.awt.ComposeWindow
import com.sun.jna.Native
import com.sun.jna.platform.win32.WinDef
import org.jetbrains.skiko.SkiaLayer
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import javax.swing.JComponent
import javax.swing.JFrame

internal val JFrame.windowHWND
    get() = WinDef.HWND(Native.getWindowPointer(this))

internal fun ComposeWindow.setComposeLayerTransparency(isTransparent: Boolean) {
    val delegate = delegateField.get(this@setComposeLayerTransparency)
    val layer = getLayerMethod.invoke(delegate)
    val component = getComponentMethod.invoke(layer) as SkiaLayer

    component.transparency = isTransparent
}

internal fun JFrame.hackContentPane() {
    // Create hacked content pane the same way of AWT
    val newContentPane: JComponent = HackedContentPane()
    newContentPane.name = "$name.contentPane"
    newContentPane.layout = object : BorderLayout() {
        override fun addLayoutComponent(comp: Component, constraints: Any?) {
            super.addLayoutComponent(comp, constraints ?: CENTER)
        }
    }

    newContentPane.background = Color(0, 0, 0, 0)

    contentPane.components.forEach { newContentPane.add(it) }

    contentPane = newContentPane
}


private val delegateField by lazy {
    ComposeWindow::class.java.getDeclaredField("delegate").apply { isAccessible = true }
}

private val getLayerMethod by lazy {
    delegateField.type.getDeclaredMethod("getLayer").apply { isAccessible = true }
}

private val getComponentMethod by lazy {
    getLayerMethod.returnType.getDeclaredMethod("getComponent")
}