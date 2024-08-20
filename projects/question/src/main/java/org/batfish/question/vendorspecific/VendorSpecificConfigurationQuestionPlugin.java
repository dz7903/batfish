package org.batfish.question.vendorspecific;

import com.google.auto.service.AutoService;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.QuestionPlugin;

@AutoService(Plugin.class)
public class VendorSpecificConfigurationQuestionPlugin extends QuestionPlugin {

    @Override
    protected Answerer createAnswerer(Question question, IBatfish batfish) {
        return new VendorSpecificConfigurationAnswerer(question, batfish);
    }

    @Override
    protected Question createQuestion() {
        return new VendorSpecificConfigurationQuestion(
                ".*",
                NodesSpecifier.ALL
        );
    }
}
