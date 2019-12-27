package org.odk.collect.android.widgets;

import android.content.Intent;
import android.provider.MediaStore;
import android.view.View;

import androidx.annotation.NonNull;

import net.bytebuddy.utility.RandomString;

import org.javarosa.core.model.data.StringData;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.odk.collect.android.R;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.widgets.base.FileWidgetTest;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

/**
 * @author James Knight
 */
public class ImageWidgetTest extends FileWidgetTest<ImageWidget> {

    @Mock
    File file;

    private String fileName;

    @NonNull
    @Override
    public ImageWidget createWidget() {
        return new ImageWidget(activity, new QuestionDetails(formEntryPrompt, "formAnalyticsID"));
    }

    @NonNull
    @Override
    public StringData getNextAnswer() {
        return new StringData(fileName);
    }

    @Override
    public Object createBinaryData(StringData answerData) {
        return file;
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        fileName = RandomString.make();

    }

    @Override
    protected void prepareForSetAnswer() {

        when(file.exists()).thenReturn(true);
        when(file.getName()).thenReturn(fileName);
    }

    @Test
    public void buttonsShouldLaunchCorrectIntents() {
        stubAllRuntimePermissionsGranted(true);

        Intent intent = getIntentLaunchedByClick(R.id.capture_image);
        assertActionEquals(MediaStore.ACTION_IMAGE_CAPTURE, intent);

        intent = getIntentLaunchedByClick(R.id.choose_image);
        assertActionEquals(Intent.ACTION_GET_CONTENT, intent);
        assertTypeEquals("image/*", intent);
    }

    @Test
    public void buttonsShouldNotLaunchIntentsWhenPermissionsDenied() {
        stubAllRuntimePermissionsGranted(false);

        assertIntentNotStarted(activity, getIntentLaunchedByClick(R.id.capture_image));
    }

    @Test
    public void usingReadOnlyOptionShouldMakeAllClickableElementsDisabled() {
        when(formEntryPrompt.isReadOnly()).thenReturn(true);

        assertThat(getWidget().captureButton.getVisibility(), is(View.GONE));
        assertThat(getWidget().chooseButton.getVisibility(), is(View.GONE));
    }
}