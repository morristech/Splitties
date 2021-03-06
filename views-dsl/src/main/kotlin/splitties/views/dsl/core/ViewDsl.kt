/*
 * Copyright (c) 2018. Louis Cognault Ayeva Derman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package splitties.views.dsl.core

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.annotation.StyleRes
import splitties.experimental.InternalSplittiesApi
import splitties.views.inflate
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/** Called so to remind that function references (that are inlined) are recommended for [view]. */
typealias NewViewRef<V> = (Context) -> V

const val NO_THEME = 0

@Suppress("NOTHING_TO_INLINE")
inline fun Context.withTheme(theme: Int) = ContextThemeWrapper(this, theme)

fun Context.wrapCtxIfNeeded(theme: Int): Context {
    return if (theme == NO_THEME) this else withTheme(theme)
}

inline fun <V : View> Context.view(
    createView: NewViewRef<V>,
    @IdRes id: Int = View.NO_ID,
    @StyleRes theme: Int = NO_THEME,
    initView: V.() -> Unit = {}
): V {
    contract { callsInPlace(initView, InvocationKind.EXACTLY_ONCE) }
    return createView(wrapCtxIfNeeded(theme)).also { it.id = id }.apply(initView)
}

inline fun <V : View> View.view(
    createView: NewViewRef<V>,
    @IdRes id: Int = View.NO_ID,
    @StyleRes theme: Int = NO_THEME,
    initView: V.() -> Unit = {}
): V {
    contract { callsInPlace(initView, InvocationKind.EXACTLY_ONCE) }
    return context.view(createView, id, theme, initView)
}

inline fun <V : View> Ui.view(
    createView: NewViewRef<V>,
    @IdRes id: Int = View.NO_ID,
    @StyleRes theme: Int = NO_THEME,
    initView: V.() -> Unit = {}
): V {
    contract { callsInPlace(initView, InvocationKind.EXACTLY_ONCE) }
    return ctx.view(createView, id, theme, initView)
}

private const val VIEW_FACTORY = "splitties:views.dsl:viewfactory"

@InternalSplittiesApi
val Context.viewFactory: ViewFactory
    @SuppressLint("WrongConstant")
    get() = try {
        getSystemService(VIEW_FACTORY) as ViewFactory? ?: ViewFactory.appInstance
    } catch (t: Throwable) {
        ViewFactory.appInstance
    }

@InternalSplittiesApi
fun Context.withViewFactory(viewFactory: ViewFactory): Context = object : ContextWrapper(this) {
    override fun getSystemService(name: String): Any? = when (name) {
        VIEW_FACTORY -> viewFactory
        else -> super.getSystemService(name)
    }
}

/**
 * Most of the time, you should use a non [InternalSplittiesApi] overload of this function that
 * takes a function of type `(Context) -> V` where V is a `View` or one of its subtypes, where
 * using a reference to the constructor (e.g. `v(::MapView)`) is possible.
 *
 * This function is meant to be used when the type of View [V] is supported by an installed
 * [ViewFactory]. This inline function is usually hidden under other inline functions such as
 * the function [textView], which define the type.
 */
@InternalSplittiesApi
inline fun <reified V : View> Context.view(
    @IdRes id: Int = View.NO_ID,
    @StyleRes theme: Int = NO_THEME,
    initView: V.() -> Unit = {}
): V {
    contract { callsInPlace(initView, InvocationKind.EXACTLY_ONCE) }
    return viewFactory(V::class.java, wrapCtxIfNeeded(theme)).also {
        it.id = id
    }.apply(initView)
}

/**
 * Most of the time, you should use a non [InternalSplittiesApi] overload of this function that
 * takes a function of type `(Context) -> V` where V is a `View` or one of its subtypes, where
 * using a reference to the constructor (e.g. `v(::MapView)`) is possible.
 *
 * This function is meant to be used when the type of View [V] is supported by an installed
 * [ViewFactory]. This inline function is usually hidden under other inline functions such as
 * the function [textView], which define the type.
 */
@InternalSplittiesApi
inline fun <reified V : View> View.view(
    @IdRes id: Int = View.NO_ID,
    @StyleRes theme: Int = NO_THEME,
    initView: V.() -> Unit = {}
): V {
    contract { callsInPlace(initView, InvocationKind.EXACTLY_ONCE) }
    return context.view(id, theme, initView)
}

/**
 * Most of the time, you should use a non [InternalSplittiesApi] overload of this function that
 * takes a function of type `(Context) -> V` where V is a `View` or one of its subtypes, where
 * using a reference to the constructor (e.g. `v(::MapView)`) is possible.
 *
 * This function is meant to be used when the type of View [V] is supported by an installed
 * [ViewFactory]. This inline function is usually hidden under other inline functions such as
 * the function [textView], which define the type.
 */
@InternalSplittiesApi
inline fun <reified V : View> Ui.view(
    @IdRes id: Int = View.NO_ID,
    @StyleRes theme: Int = NO_THEME,
    initView: V.() -> Unit = {}
): V {
    contract { callsInPlace(initView, InvocationKind.EXACTLY_ONCE) }
    return ctx.view(id, theme, initView)
}

@PublishedApi
internal const val XML_DEFINED_ID = -1

inline fun <reified V : View> Context.inflate(
    @LayoutRes layoutResId: Int,
    @IdRes id: Int = XML_DEFINED_ID,
    @StyleRes theme: Int = NO_THEME,
    initView: V.() -> Unit = {}
): V {
    contract { callsInPlace(initView, InvocationKind.EXACTLY_ONCE) }
    return wrapCtxIfNeeded(theme).inflate<V>(layoutResId).also { inflatedView ->
        if (id != XML_DEFINED_ID) inflatedView.id = id
    }.apply(initView)
}

inline fun <reified V : View> View.inflate(
    @LayoutRes layoutResId: Int,
    @IdRes id: Int = XML_DEFINED_ID,
    @StyleRes theme: Int = NO_THEME,
    initView: V.() -> Unit = {}
): V {
    contract { callsInPlace(initView, InvocationKind.EXACTLY_ONCE) }
    return context.inflate(layoutResId, id, theme, initView)
}

inline fun <reified V : View> Ui.inflate(
    @LayoutRes layoutResId: Int,
    @IdRes id: Int = XML_DEFINED_ID,
    @StyleRes theme: Int = NO_THEME,
    initView: V.() -> Unit = {}
): V {
    contract { callsInPlace(initView, InvocationKind.EXACTLY_ONCE) }
    return ctx.inflate(layoutResId, id, theme, initView)
}

@Suppress("NOTHING_TO_INLINE")
inline fun <V : View> ViewGroup.add(
    view: V,
    lp: ViewGroup.LayoutParams
): V = view.also { addView(it, lp) }
