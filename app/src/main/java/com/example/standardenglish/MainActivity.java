package com.example.standardenglish;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.AnimateBuilder;
import com.aldebaran.qi.sdk.builder.AnimationBuilder;
import com.aldebaran.qi.sdk.builder.ChatBuilder;
import com.aldebaran.qi.sdk.builder.QiChatbotBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.builder.TopicBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.*;
import com.aldebaran.qi.sdk.object.actuation.Animate;
import com.aldebaran.qi.sdk.object.actuation.Animation;
import com.aldebaran.qi.sdk.object.conversation.AutonomousReactionValidity;
import com.aldebaran.qi.sdk.object.conversation.BaseQiChatExecutor;
import com.aldebaran.qi.sdk.object.conversation.Bookmark;
import com.aldebaran.qi.sdk.object.conversation.Chat;
import com.aldebaran.qi.sdk.object.conversation.QiChatbot;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.object.conversation.Topic;
import com.aldebaran.qi.sdk.object.locale.Language;
import com.aldebaran.qi.sdk.object.locale.Locale;
import com.aldebaran.qi.sdk.object.locale.Region;
import com.aldebaran.qi.sdk.object.conversation.QiChatExecutor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.aldebaran.qi.sdk.object.conversation.AutonomousReactionImportance.HIGH;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class MainActivity extends RobotActivity implements RobotLifecycleCallbacks {

    private static final String TAG ="MYCODE: " ;
    private Chat chat;
    private Animate animate;
    private Future<Void> currentChatFuture;
    Locale locale = new Locale(Language.ENGLISH, Region.UNITED_STATES);

    private QiContext qiContext;
    private QiChatbot qiChatbot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Register the RobotLifecycleCallbacks to this Activity.
        QiSDK.register(this, this);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onDestroy() {
        // Unregister the RobotLifecycleCallbacks for this Activity.
        QiSDK.unregister(this, this);
        super.onDestroy();
    }

    private Chat buildChat(QiContext qiContext, String topicAssetName,
                           Locale locale) {
        // Create a topic from the asset file.
        Topic topic = TopicBuilder.with(qiContext)
                .withAsset(topicAssetName)
                .build();
        Bookmark startBookmark = topic.getBookmarks().get("start");

        // Create a new QiChatbot with the specified Locale.
        QiChatbot qiChatbot = QiChatbotBuilder.with(qiContext)
                .withTopic(topic)
                .withLocale(locale)
                .build();

        // Create a new Chat action with the specified Locale.
        Chat chat = ChatBuilder.with(qiContext)
                .withChatbot(qiChatbot)
                .withLocale(locale)
                .build();

        chat.addOnStartedListener(() -> {
            Log.i(TAG, "Discussion is now in " + locale.getLanguage() + ".");
            qiChatbot.async().goToBookmark(startBookmark, HIGH, AutonomousReactionValidity.IMMEDIATE);
        });

        return chat;
    }



    private class MyQiChatExecutor extends BaseQiChatExecutor {
        private final QiContext qiContext;
        private Future<Void> animationFuture;

        MyQiChatExecutor(QiContext context) {
            super(context);
            this.qiContext = context;
        }

        @Override
        public void runWith(List<String> params) {
            String param = params.get(0);
            animate(qiContext, qiContext.getResources().getIdentifier(param, "raw", qiContext.getPackageName()));
        }

        @Override
        public void stop() {
        }

        private void animate(QiContext qiContext,int resource) {
            // Create an animation.
            Animation animation = AnimationBuilder.with(qiContext) // Create the builder with the context.
                    .withResources(resource) // Set the animation resource.
                    .build(); // Build the animation.

            // Create an animate action.
            Animate animate = AnimateBuilder.with(qiContext) // Create the builder with the context.
                    .withAnimation(animation) // Set the animation.
                    .build(); // Build the animate action.

            animate.run();
        }
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        // Create a new say action.
        Say say = SayBuilder.with(qiContext) // Create the builder with the context.
                .withText("Hi there. Welcome to Aarhus University") // Set the text to say.
                .build(); // Build the say action.

        say.run();

        Topic topic = TopicBuilder.with(qiContext) // Create the builder using the QiContext.
                .withResource(R.raw.welcome) // Set the topic resource.
                .build(); // Build the topic.

        // Create a new QiChatbot.
        QiChatbot qiChatbot = QiChatbotBuilder.with(qiContext)
                .withTopic(topic)
                .withLocale(locale)
                .build();

        Map<String, QiChatExecutor> executors = new HashMap<>();
        executors.put("myExecutor", new MyQiChatExecutor(qiContext));
        qiChatbot.setExecutors(executors);

        chat = ChatBuilder.with(qiContext)
                .withChatbot(qiChatbot)
                .withLocale(locale)
                .build();

// Add an on started listener to the Chat action.
        chat.addOnStartedListener(() -> Log.i(TAG, "Discussion started."));
        chat.async().run();
    }

    @Override
    public void onRobotFocusLost() {
        // The robot focus is lost.
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        // The robot focus is refused.
    }
}