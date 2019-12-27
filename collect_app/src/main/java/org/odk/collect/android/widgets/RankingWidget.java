/*
 * Copyright 2018 Nafundi
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

package org.odk.collect.android.widgets;

import android.content.Context;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectMultiData;
import org.javarosa.core.model.data.helper.Selection;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.fragments.dialogs.RankingWidgetDialog;
import org.odk.collect.android.logic.FormController;
import org.odk.collect.android.widgets.interfaces.BinaryWidget;
import org.odk.collect.android.widgets.warnings.SpacesInUnderlyingValuesWarning;

import java.util.ArrayList;
import java.util.List;

public class RankingWidget extends ItemsWidget implements BinaryWidget {

    private List<SelectChoice> savedItems;
    private LinearLayout widgetLayout;
    Button showRankingDialogButton;

    public RankingWidget(Context context, QuestionDetails prompt) {
        super(context, prompt);

        setUpLayout(getOrderedItems());
    }

    @Override
    public IAnswerData getAnswer() {
        List<Selection> orderedItems = new ArrayList<>();
        if (savedItems != null) {
            for (SelectChoice selectChoice : savedItems) {
                orderedItems.add(new Selection(selectChoice));
            }
        }

        return orderedItems.isEmpty() ? null : new SelectMultiData(orderedItems);
    }

    @Override
    public void clearAnswer() {
        savedItems = null;
        setUpLayout(items);
    }

    @Override
    public void setFocus(Context context) {
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        showRankingDialogButton.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        showRankingDialogButton.cancelLongPress();
    }

    @Override
    public void setBinaryData(Object values) {
        savedItems = (List<SelectChoice>) values;
        setUpLayout(savedItems);
    }

    @Override
    public void onButtonClick(int buttonId) {
        FormController formController = Collect.getInstance().getFormController();
        if (formController != null) {
            formController.setIndexWaitingForData(getFormEntryPrompt().getIndex());
        }
        RankingWidgetDialog rankingWidgetDialog = RankingWidgetDialog.newInstance(savedItems == null ? items : savedItems, getFormEntryPrompt().getIndex());
        rankingWidgetDialog.show(((FormEntryActivity) getContext()).getSupportFragmentManager(), "RankingDialog");
    }

    private List<SelectChoice> getOrderedItems() {
        List<Selection> savedOrderedItems =
                getFormEntryPrompt().getAnswerValue() == null
                ? new ArrayList<>()
                : (List<Selection>) getFormEntryPrompt().getAnswerValue().getValue();

        if (savedOrderedItems.isEmpty()) {
            return items;
        } else {
            savedItems = new ArrayList<>();
            for (Selection selection : savedOrderedItems) {
                for (SelectChoice selectChoice : items) {
                    if (selection.getValue().equals(selectChoice.getValue())) {
                        savedItems.add(selectChoice);
                        break;
                    }
                }
            }

            for (SelectChoice selectChoice : items) {
                if (!savedItems.contains(selectChoice)) {
                    savedItems.add(selectChoice);
                }
            }

            return savedItems;
        }
    }

    private void setUpLayout(List<SelectChoice> items) {
        removeView(widgetLayout);

        widgetLayout = new LinearLayout(getContext());
        widgetLayout.setOrientation(LinearLayout.VERTICAL);
        showRankingDialogButton = getSimpleButton(getContext().getString(R.string.rank_items));
        widgetLayout.addView(showRankingDialogButton);
        widgetLayout.addView(setUpAnswerTextView());

        addAnswerView(widgetLayout);
        SpacesInUnderlyingValuesWarning
                .forQuestionWidget(this)
                .renderWarningIfNecessary(items);
    }

    private TextView setUpAnswerTextView() {
        StringBuilder answerText = new StringBuilder();
        if (savedItems != null) {
            for (SelectChoice item : savedItems) {
                answerText
                        .append(savedItems.indexOf(item) + 1)
                        .append(". ")
                        .append(getFormEntryPrompt().getSelectChoiceText(item));
                if ((savedItems.size() - 1) > savedItems.indexOf(item)) {
                    answerText.append('\n');
                }
            }
        }
        return getAnswerTextView(answerText.toString());
    }
}
