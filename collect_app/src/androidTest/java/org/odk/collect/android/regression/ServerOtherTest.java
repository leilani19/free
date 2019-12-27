package org.odk.collect.android.regression;

import androidx.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.support.pages.MainMenuPage;
import org.odk.collect.android.support.ResetStateRule;

// Issue number NODK-235
@RunWith(AndroidJUnit4.class)
public class ServerOtherTest extends BaseRegressionTest {

    @Rule
    public RuleChain ruleChain = RuleChain
            .outerRule(new ResetStateRule());

    @Test
    public void formListPath_ShouldBeUpdated() {
        //TestCase1
        new MainMenuPage(rule)
                .clickOnMenu()
                .clickGeneralSettings()
                .openServerSettings()
                .clickOnServerType()
                .clickOnAreaWithIndex("CheckedTextView", 2)
                .clickFormListPath()
                .addText("/formList", "/sialala")
                .clickOKOnDialog()
                .checkIsTextDisplayed("/formList/sialala");
    }

    @Test
    public void submissionsPath_ShouldBeUpdated() {
        //TestCase2
        new MainMenuPage(rule)
                .clickOnMenu()
                .clickGeneralSettings()
                .openServerSettings()
                .clickOnServerType()
                .clickOnAreaWithIndex("CheckedTextView", 2)
                .clickSubmissionPath()
                .addText("/submission", "/blabla")
                .clickOKOnDialog()
                .checkIsTextDisplayed("/submission/blabla");
    }

}
