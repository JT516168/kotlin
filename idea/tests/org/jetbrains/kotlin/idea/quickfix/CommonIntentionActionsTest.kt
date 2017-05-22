/*
 * Copyright 2010-2017 JetBrains s.r.o.
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

package org.jetbrains.kotlin.idea.quickfix

import com.intellij.codeInsight.intention.JvmCommonIntentionActionsFactory
import com.intellij.lang.Language
import com.intellij.psi.PsiModifier
import com.intellij.psi.PsiType
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import org.jetbrains.uast.*
import org.junit.Assert


class CommonIntentionActionsTest : LightPlatformCodeInsightFixtureTestCase() {

    fun testMakeNotFinal() {
        myFixture.configureByText("foo.kt", """
        class Foo {
            fun bar<caret>(){}
        }
        """)

        myFixture.launchAction(codeModifications.createChangeModifierAction(atCaret<UDeclaration>(myFixture), PsiModifier.FINAL, false)!!)
        myFixture.checkResult("""
        class Foo {
            open fun bar(){}
        }
        """)
    }

    fun testMakePrivate() {
        myFixture.configureByText("foo.kt", """
        class Foo<caret> {
            fun bar(){}
        }
        """)

        myFixture.launchAction(codeModifications.createChangeModifierAction(atCaret<UDeclaration>(myFixture), PsiModifier.PRIVATE, true)!!)
        myFixture.checkResult("""
        private class Foo {
            fun bar(){}
        }
        """)
    }

    fun testMakeNotPrivate() {
        myFixture.configureByText("foo.kt", """
        private class Foo<caret> {
            fun bar(){}
        }
        """.trim())

        myFixture.launchAction(codeModifications.createChangeModifierAction(atCaret<UDeclaration>(myFixture), PsiModifier.PRIVATE, false)!!)
        myFixture.checkResult("""
        class Foo {
            fun bar(){}
        }
        """.trim(), true)
    }

    fun testDontMakeFunInObjectsOpen() {
        myFixture.configureByText("foo.kt", """
        object Foo {
            fun bar<caret>(){}
        }
        """.trim())
        Assert.assertNull(codeModifications.createChangeModifierAction(atCaret<UDeclaration>(myFixture), PsiModifier.FINAL, false))
    }

    fun testAddVoidVoidMethod() {
        myFixture.configureByText("foo.kt", """
        |class Foo<caret> {
        |    fun bar() {}
        |}
        """.trim().trimMargin())

        myFixture.launchAction(codeModifications.createAddMethodAction(
                atCaret<UClass>(myFixture), "baz", PsiModifier.PRIVATE, PsiType.VOID)!!)
        myFixture.checkResult("""
        |class Foo {
        |    fun bar() {}
        |    private fun baz() {}
        |}
        """.trim().trimMargin(), true)
    }

    fun testAddIntIntMethod() {
        myFixture.configureByText("foo.kt", """
        |class Foo<caret> {
        |    fun bar() {}
        |}
        """.trim().trimMargin())

        myFixture.launchAction(codeModifications.createAddMethodAction(
                atCaret<UClass>(myFixture), "baz", PsiModifier.PUBLIC, PsiType.INT, PsiType.INT)!!)
        myFixture.checkResult("""
        |class Foo {
        |    fun bar() {}
        |    fun baz(arg1: Int): Int {}
        |}
        """.trim().trimMargin(), true)
    }

    fun testAddStringVarProperty() {
        myFixture.configureByText("foo.kt", """
        |class Foo<caret> {
        |    fun bar() {}
        |}
        """.trim().trimMargin())

        myFixture.launchAction(codeModifications.createAddBeanPropertyActions(
                atCaret<UClass>(myFixture), "baz", PsiModifier.PUBLIC, PsiType.getTypeByName("java.lang.String", project, GlobalSearchScope.allScope(project)), true, true).first())
        myFixture.checkResult("""
        |class Foo {
        |    var baz: String
        |    fun bar() {}
        |}
        """.trim().trimMargin(), true)
    }

    fun testAddStringValProperty() {
        myFixture.configureByText("foo.kt", """
        |class Foo<caret> {
        |    fun bar() {}
        |}
        """.trim().trimMargin())

        myFixture.launchAction(codeModifications.createAddBeanPropertyActions(
                atCaret<UClass>(myFixture), "baz", PsiModifier.PUBLIC, PsiType.getTypeByName("java.lang.String", project, GlobalSearchScope.allScope(project)), true, false).first())
        myFixture.checkResult("""
        |class Foo {
        |    val baz: String
        |    fun bar() {}
        |}
        """.trim().trimMargin(), true)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : UElement> atCaret(myFixture: CodeInsightTestFixture): T {
        return myFixture.elementAtCaret.toUElement() as T
    }

    private val codeModifications: JvmCommonIntentionActionsFactory
        get() = JvmCommonIntentionActionsFactory.forLanguage(Language.findLanguageByID("kotlin")!!)!!

}

