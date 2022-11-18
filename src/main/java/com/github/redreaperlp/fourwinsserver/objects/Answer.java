package com.github.redreaperlp.fourwinsserver.objects;

import com.github.redreaperlp.fourwinsserver.objects.enums.ServerAnswer;

public class Answer {
    private ServerAnswer answer;
    private String answerData;

    public Answer(ServerAnswer answer, String answerData) {
        this.answer = answer;
        this.answerData = answerData;
    }

    public ServerAnswer getAnswer() {
        return answer;
    }

    public String getAnswerData() {
        return answerData;
    }

    public void setAnswer(ServerAnswer answer) {
        this.answer = answer;
    }

    public void setAnswerData(String answerData) {
        this.answerData = answerData;
    }
}
