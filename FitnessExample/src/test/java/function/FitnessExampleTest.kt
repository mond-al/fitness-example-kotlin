package function

import fitnesse.wiki.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is
import org.junit.Before
import org.junit.Test

class FitnessExampleTest {
    private var pageData: PageData? = null
    private var crawler: PageCrawler? = null
    private var root: WikiPage? = null
    private var testPage: WikiPage? = null
    private val expectedResultForTestCase = """<div class="setup">
	<div style="float: right;" class="meta"><a href="javascript:expandAll();">Expand All</a> | <a href="javascript:collapseAll();">Collapse All</a></div>
	<a href="javascript:toggleCollapsable('');">
		<img src="/files/images/collapsableOpen.gif" class="left" id="img"/>
	</a>
&nbsp;<span class="meta">Set Up: <a href="SuiteSetUp">.SuiteSetUp</a> <a href="SuiteSetUp?edit&amp;redirectToReferer=true&amp;redirectAction=">(edit)</a></span>
	<div class="collapsable" id="">suiteSetUp</div>
</div>
<div class="setup">
	<div style="float: right;" class="meta"><a href="javascript:expandAll();">Expand All</a> | <a href="javascript:collapseAll();">Collapse All</a></div>
	<a href="javascript:toggleCollapsable('');">
		<img src="/files/images/collapsableOpen.gif" class="left" id="img"/>
	</a>
&nbsp;<span class="meta">Set Up: <a href="SetUp">.SetUp</a> <a href="SetUp?edit&amp;redirectToReferer=true&amp;redirectAction=">(edit)</a></span>
	<div class="collapsable" id="">setup</div>
</div>
<span class="meta">variable defined: TEST_SYSTEM=slim</span><br/>the content!include -teardown <a href="TearDown">.TearDown</a><br/><div class="teardown">
	<div style="float: right;" class="meta"><a href="javascript:expandAll();">Expand All</a> | <a href="javascript:collapseAll();">Collapse All</a></div>
	<a href="javascript:toggleCollapsable('');">
		<img src="/files/images/collapsableOpen.gif" class="left" id="img"/>
	</a>
&nbsp;<span class="meta">Tear Down: <a href="SuiteTearDown">.SuiteTearDown</a> <a href="SuiteTearDown?edit&amp;redirectToReferer=true&amp;redirectAction=">(edit)</a></span>
	<div class="collapsable" id="">suiteTearDown</div>
</div>
"""
    private var expectedResultForNonTestCase = """<div class="setup">
	<div style="float: right;" class="meta"><a href="javascript:expandAll();">Expand All</a> | <a href="javascript:collapseAll();">Collapse All</a></div>
	<a href="javascript:toggleCollapsable('');">
		<img src="/files/images/collapsableOpen.gif" class="left" id="img"/>
	</a>
&nbsp;<span class="meta">Set Up: <a href="SetUp">.SetUp</a> <a href="SetUp?edit&amp;redirectToReferer=true&amp;redirectAction=">(edit)</a></span>
	<div class="collapsable" id="">setup</div>
</div>
<div class="setup">
	<div style="float: right;" class="meta"><a href="javascript:expandAll();">Expand All</a> | <a href="javascript:collapseAll();">Collapse All</a></div>
	<a href="javascript:toggleCollapsable('');">
		<img src="/files/images/collapsableOpen.gif" class="left" id="img"/>
	</a>
&nbsp;<span class="meta">Set Up: <a href="SuiteSetUp">.SuiteSetUp</a> <a href="SuiteSetUp?edit&amp;redirectToReferer=true&amp;redirectAction=">(edit)</a></span>
	<div class="collapsable" id="">suiteSetUp</div>
</div>
<div class="setup">
	<div style="float: right;" class="meta"><a href="javascript:expandAll();">Expand All</a> | <a href="javascript:collapseAll();">Collapse All</a></div>
	<a href="javascript:toggleCollapsable('');">
		<img src="/files/images/collapsableOpen.gif" class="left" id="img"/>
	</a>
&nbsp;<span class="meta">Set Up: <a href="SetUp">.SetUp</a> <a href="SetUp?edit&amp;redirectToReferer=true&amp;redirectAction=">(edit)</a></span>
	<div class="collapsable" id="">setup</div>
</div>
<span class="meta">variable defined: TEST_SYSTEM=slim</span><br/>the content!include -teardown <a href="TearDown">.TearDown</a><br/><div class="teardown">
	<div style="float: right;" class="meta"><a href="javascript:expandAll();">Expand All</a> | <a href="javascript:collapseAll();">Collapse All</a></div>
	<a href="javascript:toggleCollapsable('');">
		<img src="/files/images/collapsableOpen.gif" class="left" id="img"/>
	</a>
&nbsp;<span class="meta">Tear Down: <a href="SuiteTearDown">.SuiteTearDown</a> <a href="SuiteTearDown?edit&amp;redirectToReferer=true&amp;redirectAction=">(edit)</a></span>
	<div class="collapsable" id="">suiteTearDown</div>
</div>
<div class="teardown">
	<div style="float: right;" class="meta"><a href="javascript:expandAll();">Expand All</a> | <a href="javascript:collapseAll();">Collapse All</a></div>
	<a href="javascript:toggleCollapsable('');">
		<img src="/files/images/collapsableOpen.gif" class="left" id="img"/>
	</a>
&nbsp;<span class="meta">Tear Down: <a href="TearDown">.TearDown</a> <a href="TearDown?edit&amp;redirectToReferer=true&amp;redirectAction=">(edit)</a></span>
	<div class="collapsable" id="">teardown</div>
</div>
"""

    @Before
    @Throws(Exception::class)
    fun setUp() {
        root = InMemoryPage.makeRoot("RooT")
        crawler = root!!.pageCrawler
        testPage = addPage(
            "TestPage", """
     !define TEST_SYSTEM {slim}
     the content
     """.trimIndent()
        )
        addPage("SetUp", "setup")
        addPage("TearDown", "teardown")
        addPage("SuiteSetUp", "suiteSetUp")
        addPage("SuiteTearDown", "suiteTearDown")
        crawler!!.addPage(testPage, PathParser.parse("ScenarioLibrary"), "scenario library 2")
        pageData = testPage!!.data
    }

    private fun includeSuiteSetup(b: Boolean): Boolean {
        return b
    }

    private fun removeMagicNumber(expectedResult: String): String {
        return expectedResult.replace("[-]*\\d+".toRegex(), "")
    }

    @Throws(Exception::class)
    private fun addPage(pageName: String, content: String): WikiPage {
        return crawler!!.addPage(root, PathParser.parse(pageName), content)
    }

    @Test
    @Throws(Exception::class)
    fun testableHtml() {
        val expectedResult = removeMagicNumber(expectedResultForTestCase)
        var testableHtml = FitnessExample().testableHtml(pageData!!, includeSuiteSetup(true))
        testableHtml = removeMagicNumber(testableHtml)
        assertThat(testableHtml, Is.`is`(expectedResult))
        testableHtml = FitnessExample().testableHtml(pageData!!, includeSuiteSetup(false))
        testableHtml = removeMagicNumber(testableHtml)
        expectedResultForNonTestCase = removeMagicNumber(expectedResultForNonTestCase)
        assertThat(testableHtml, Is.`is`(expectedResultForNonTestCase))
    }
}