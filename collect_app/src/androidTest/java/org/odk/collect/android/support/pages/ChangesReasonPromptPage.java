package org.odk.collect.android.support.pages;

import androidx.test.rule.ActivityTestRule;

import org.odk.collect.android.R;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

public class ChangesReasonPromptPage extends Page<ChangesReasonPromptPage> {

    private final String formName;

    public ChangesReasonPromptPage(String formName, ActivityTestRule rule) {
        super(rule);
        this.formName = formName;
    }

    @Override
    public ChangesReasonPromptPage assertOnPage() {
        onView(allOf(withText(formName), isDescendantOfA(withId(R.id.toolbar)))).check(matches(isDisplayed()));
        onView(withText(getTranslatedString(R.string.reason_for_changes))).check(matches(isDisplayed()));
        return this;
    }

    public ChangesReasonPromptPage enterReason(String reason) {
        onView(withHint(getTranslatedString(R.string.reason))).perform(replaceText(reason));
        return this;
    }

    public MainMenuPage clickSave() {
        clickOnString(R.string.save);
        return new MainMenuPage(rule).assertOnPage();
    }

    public <D extends Page<D>> D pressClose(D destination) {
        onView(withContentDescription(getTranslatedString(R.string.close))).perform(click());
        return destination.assertOnPage();
    }

    public ChangesReasonPromptPage clickSaveWithValidationError() {
        clickOnString(R.string.save);
        return this.assertOnPage();
    }
}
