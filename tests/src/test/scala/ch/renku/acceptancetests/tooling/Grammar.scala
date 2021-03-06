package ch.renku.acceptancetests.tooling

import ch.renku.acceptancetests.pages.Page
import org.openqa.selenium.WebElement
import org.scalatest.concurrent.Eventually
import org.scalatestplus.selenium
import org.scalatestplus.selenium.WebBrowser

import scala.concurrent.duration.Duration
import scala.jdk.CollectionConverters._
import scala.language.implicitConversions

trait Grammar extends Eventually {
  self: WebBrowser with AcceptanceSpec =>

  implicit def toSeleniumPage[Url <: BaseUrl](page: Page[Url])(implicit baseUrl: Url): selenium.Page =
    new selenium.Page {
      override val url = page.url
    }

  object sleep {
    def apply(duration: Duration): Unit = Page.SleepThread(duration)
  }

  object verify {

    def that(element: => WebElement): WebElement = element

    def browserAt[Url <: BaseUrl](page: Page[Url])(implicit baseUrl: Url): Unit = eventually {
      currentUrl should startWith(page.url)
      pageTitle  shouldBe page.title.toString()
    }

    def browserSwitchedTo[Url <: BaseUrl](page: Page[Url])(implicit baseUrl: Url): Unit = eventually {
      if (webDriver.getWindowHandles.asScala exists forTabWith(page)) ()
      else throw new Exception(s"Cannot find window with ${page.url}")
    }

    private def forTabWith[Url <: BaseUrl](page: Page[Url])(handle: String)(implicit baseUrl: Url): Boolean = {
      webDriver.switchTo() window handle
      (currentUrl startsWith page.url) && (pageTitle == page.title.toString())
    }

    def userCanSee(element: => WebElement): Unit = eventually {
      element.isDisplayed shouldBe true
    }
  }

  def unless(test: Boolean)(testFun: => Any): Unit =
    if (!test) testFun

  protected implicit class WebElementGrammar(element: WebElement) {
    def is(expected:       String): Unit = element.getText               shouldBe expected
    def contains(expected: String): Unit = element.getText               should include(expected)
    def matches(pattern:   String): Unit = element.getText               should fullyMatch regex pattern
    def hasValue(expected: String): Unit = element.getAttribute("value") shouldBe expected
  }

  protected implicit class OperationOps(unit: Unit) {
    def sleep(duration: Duration): Unit = Page.SleepThread(duration)
  }
}
