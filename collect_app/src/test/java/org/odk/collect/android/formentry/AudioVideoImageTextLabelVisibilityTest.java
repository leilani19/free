package org.odk.collect.android.formentry;

import android.view.View;
import android.widget.TextView;

import junit.framework.Assert;

import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.ReferenceManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.audio.AudioHelper;
import org.odk.collect.android.formentry.questions.AudioVideoImageTextLabel;
import org.odk.collect.android.logic.FileReference;
import org.odk.collect.android.support.RobolectricHelpers;
import org.odk.collect.android.support.TestScreenContextActivity;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.Arrays;
import java.util.Random;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class AudioVideoImageTextLabelVisibilityTest {

    private static final String RANDOM_URI = "randomMediaURI";

    private final String audioURI;
    private final String imageURI;
    private final String videoURI;

    private ReferenceManager referenceManager;
    private FileReference reference;

    private AudioVideoImageTextLabel audioVideoImageTextLabel;
    private View audioButton;
    private View videoButton;
    private View imageView;
    private View missingImage;
    private TextView textView;
    private boolean isReferenceManagerStubbed;
    private AudioHelper audioHelper;

    public AudioVideoImageTextLabelVisibilityTest(String audioURI, String imageURI, String videoURI) {
        this.audioURI = audioURI;
        this.imageURI = imageURI;
        this.videoURI = videoURI;
    }

    @ParameterizedRobolectricTestRunner.Parameters()
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {null, null, null},
                {RANDOM_URI, null, null},
                {null, RANDOM_URI, null},
                {null, null, RANDOM_URI},
                {RANDOM_URI, RANDOM_URI, null},
                {RANDOM_URI, null, RANDOM_URI},
                {null, RANDOM_URI, RANDOM_URI},
                {RANDOM_URI, RANDOM_URI, RANDOM_URI}
        });
    }

    @Before
    public void setUp() throws InvalidReferenceException {
        reference = mock(FileReference.class);
        referenceManager = mock(ReferenceManager.class);
        textView = new TextView(RuntimeEnvironment.application);

        TestScreenContextActivity activity = RobolectricHelpers.createThemedActivity(TestScreenContextActivity.class);
        audioHelper = new AudioHelper(activity, activity.getViewLifecycle());

        audioVideoImageTextLabel = new AudioVideoImageTextLabel(activity);

        audioButton = audioVideoImageTextLabel.findViewById(R.id.audioButton);
        videoButton = audioVideoImageTextLabel.findViewById(R.id.videoButton);
        imageView = audioVideoImageTextLabel.findViewById(R.id.imageView);
        missingImage = audioVideoImageTextLabel.findViewById(R.id.missingImage);

        /*
         * Stub reference manager randomly to account for both illegal URI and proper URI while
         * attempting to load image view
         */
        if (new Random().nextBoolean()) {
            stubReferenceManager();
        } else {
            when(referenceManager.deriveReference(RANDOM_URI)).thenThrow(InvalidReferenceException.class);
        }
    }

    @Test
    public void viewShouldBecomeVisibleIfUriPresent() {
        Assert.assertNotNull(audioVideoImageTextLabel);
        Assert.assertEquals(VISIBLE, audioVideoImageTextLabel.getVisibility());
        assertVisibility(GONE, audioButton, videoButton, imageView, missingImage);

        audioVideoImageTextLabel.setText(textView);
        audioVideoImageTextLabel.setImageVideo(imageURI, videoURI, null, referenceManager);
        if (audioURI != null) {
            audioVideoImageTextLabel.setAudio(audioURI, audioHelper);
        }

        // we do not check for the validity of the URIs for the audio and video while loading MediaLayout
        assertVisibility(audioURI == null ? GONE : VISIBLE, audioButton);
        assertVisibility(videoURI == null ? GONE : VISIBLE, videoButton);

        if (imageURI == null || !isReferenceManagerStubbed) {
            // either the URI wasn't provided or it encountered InvalidReferenceException
            assertVisibility(GONE, imageView, missingImage);
        } else {
            // either the bitmap was successfully loaded or the file was missing
            Assert.assertNotSame(imageView.getVisibility(), missingImage.getVisibility());
        }
    }

    /*
     * Stubbing {@link ReferenceManager} to return random file name in order to prevent
     * {@link InvalidReferenceException}
     */
    private void stubReferenceManager() throws InvalidReferenceException {
        isReferenceManagerStubbed = true;

        doReturn(reference).when(referenceManager).deriveReference(RANDOM_URI);
        doReturn(RANDOM_URI).when(reference).getLocalURI();
    }

    /**
     * @param visibility Expected visibility
     * @param views      Views whose actual visibility is to be asserted
     */
    private void assertVisibility(int visibility, View... views) {
        for (View view : views) {
            Assert.assertEquals(visibility, view.getVisibility());
        }
    }
}
