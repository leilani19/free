package org.odk.collect.android.widgets;

import android.view.MotionEvent;
import android.view.View;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.reference.ReferenceManager;
import org.mockito.Mock;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.support.MockFormEntryPromptBuilder;
import org.odk.collect.android.support.RobolectricHelpers;
import org.odk.collect.android.widgets.base.SelectWidgetTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.support.Helpers.createMockReference;

public abstract class SelectImageMapWidgetTest<W extends SelectImageMapWidget, A extends IAnswerData>
        extends SelectWidgetTest<W, A> {

    @Mock
    public ReferenceManager referenceManager;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        overrideDependencyModule();
        createMockReference(referenceManager, "jr://images/body.svg");
        formEntryPrompt = new MockFormEntryPromptBuilder()
                .withIndex("i am index")
                .withImageURI("jr://images/body.svg")
                .build();
    }

    private void overrideDependencyModule() {
        RobolectricHelpers.overrideAppDependencyModule(new AppDependencyModule() {
            @Override
            public ReferenceManager providesReferenceManager() {
                return referenceManager;
            }
        });
    }

    @Override
    public void usingReadOnlyOptionShouldMakeAllClickableElementsDisabled() {
        formEntryPrompt = new MockFormEntryPromptBuilder(formEntryPrompt)
                .withReadOnly(true)
                .build();
        MotionEvent motionEvent = mock(MotionEvent.class);
        when(motionEvent.getAction()).thenReturn(MotionEvent.ACTION_DOWN);

        assertThat(getWidget().webView.getVisibility(), is(View.VISIBLE));
        assertThat(getWidget().webView.isEnabled(), is(Boolean.FALSE));
    }
}
