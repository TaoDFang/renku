package ch.renku.acceptancetests.workflows

import ch.renku.acceptancetests.model.projects.ProjectDetails
import ch.renku.acceptancetests.pages._
import ch.renku.acceptancetests.tooling.AcceptanceSpec

import scala.language.postfixOps

trait Settings {
  self: AcceptanceSpec =>

  def setProjectTags(implicit projectDetails: ProjectDetails): Unit = {
    val projectPage = ProjectPage()
    When("the user navigates to the Settings tab")
    click on projectPage.Settings.tab
    And("they add some tags")
    val tags = "automated-test"
    projectPage.Settings addProjectTags tags
    Then("the tags should be added")
    verify that projectPage.Settings.projectTags hasValue tags
  }

  def setProjectDescription(implicit projectDetails: ProjectDetails): Unit = {
    val projectPage = ProjectPage()
    When("the user updates the Project Description")
    val addedDescription = " some added description"
    projectPage.Settings updateProjectDescription addedDescription
    And("they navigate to the Overview tab")
    click on projectPage.Overview.tab
    Then("they should see the updated project description")
    verify that projectPage.Overview.projectDescription contains addedDescription
  }
}
