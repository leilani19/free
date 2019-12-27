package org.odk.collect.android.support.pages;

import android.database.Cursor;

import androidx.test.espresso.matcher.CursorMatchers;
import androidx.test.rule.ActivityTestRule;

import org.odk.collect.android.R;
import org.odk.collect.android.provider.FormsProviderAPI.FormsColumns;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.AllOf.allOf;
import static org.odk.collect.android.test.CustomMatchers.withIndex;

public class FillBlankFormPage extends Page<FillBlankFormPage> {

    public FillBlankFormPage(ActivityTestRule rule) {
        super(rule);
    }

    @Override
    public FillBlankFormPage assertOnPage() {
        checkIsStringDisplayed(R.string.enter_data);
        return this;
    }

    public IdentifyUserPromptPage clickOnFormWithIdentityPrompt(String formName) {
        clickOnFormButton(formName);
        return new IdentifyUserPromptPage(formName, rule).assertOnPage();
    }

    public FillBlankFormPage clickOnSortByButton() {
        onView(withId(R.id.menu_sort)).perform(click());
        return this;
    }

    public FillBlankFormPage clickMenuFilter() {
        onView(withId(R.id.menu_filter)).perform(click());
        return this;
    }

    public BlankFormSearchPage searchInBar(String query) {
        onView(withId(R.id.search_src_text)).perform(replaceText(query));
        return new BlankFormSearchPage(rule).assertOnPage();
    }

    public FillBlankFormPage checkIsFormSubtextDisplayed() {
        onView(withIndex(withId(R.id.form_subtitle2), 0)).check(matches(isDisplayed()));
        return this;
    }

    public FillBlankFormPage checkMapIconDisplayedForForm(String formName) {
        onData(allOf(is(instanceOf(Cursor.class)), CursorMatchers.withRowString(FormsColumns.DISPLAY_NAME, is(formName))))
                .onChildView(withId(R.id.map_button))
                .check(matches(isDisplayed()));
        return this;
    }

    public FillBlankFormPage checkMapIconNotDisplayedForForm(String formName) {
        onData(allOf(is(instanceOf(Cursor.class)), CursorMatchers.withRowString(FormsColumns.DISPLAY_NAME, is(formName))))
                .onChildView(withId(R.id.map_button))
                .check(matches(not(isDisplayed())));
        return this;
    }

    public FormMapPage clickOnMapIconForForm(String formName) {
        onData(allOf(is(instanceOf(Cursor.class)), CursorMatchers.withRowString(FormsColumns.DISPLAY_NAME, is(formName))))
                .onChildView(withId(R.id.map_button))
                .perform(click());
        return new FormMapPage(rule).assertOnPage();
    }

    public FormEntryPage clickOnForm(String formName) {
        clickOnFormButton(formName);
        return new FormEntryPage(formName, rule);
    }

    private void clickOnFormButton(String formName) {
        onView(withText(formName)).perform(scrollTo(), click());
    }
}
