package es.ulpgc.eite.da.advquiz.cheat;

import android.util.Log;

import java.lang.ref.WeakReference;

import es.ulpgc.eite.da.advquiz.app.AppMediator;
import es.ulpgc.eite.da.advquiz.app.CheatToQuestionState;
import es.ulpgc.eite.da.advquiz.app.QuestionToCheatState;


public class CheatPresenter implements CheatContract.Presenter {

    public static String TAG = "AdvQuiz.CheatPresenter";

    private AppMediator mediator;
    private WeakReference<CheatContract.View> view;
    private CheatState state;
    private CheatContract.Model model;

    public CheatPresenter(AppMediator mediator) {
        this.mediator = mediator;
    }


    @Override
    public void onCreateCalled() {
        Log.e(TAG, "onCreateCalled");

        // Inicializar estado
        state = new CheatState();

        // Leer datos enviados por Question (transitorio, se auto-nullifica)
        QuestionToCheatState savedState = mediator.getQuestionToCheatState();
        if (savedState != null) {
            model.setCorrectAnswer(savedState.correctAnswer);
        }

        // Estado inicial: botones activados, respuesta vacía
        state.answer = model.getAnswerEmptyText();
        state.buttonEnabled = true;
        state.cheated = false;
    }

    @Override
    public void onRecreateCalled() {
        Log.e(TAG, "onRecreateCalled");

        // Restaurar estado tras rotación
        state = mediator.getCheatState();
    }

    @Override
    public void onResumeCalled() {
        Log.e(TAG, "onResumeCalled");

        // Pintar vista con estado actual
        view.get().displayAnswerData(state);
    }

    @Override
    public void onPauseCalled() {
        Log.e(TAG, "onPauseCalled");

        // Guardar estado para sobrevivir rotaciones
        mediator.setCheatState(state);
    }

    @Override
    public void onDestroyCalled() {
        Log.e(TAG, "onDestroyCalled");
    }

    @Override
    public void onBackButtonPressed() {
        Log.e(TAG, "onBackButtonPressed");

        // Enviar a Question si el usuario vio la respuesta o no
        CheatToQuestionState cheatToQuestion = new CheatToQuestionState();
        cheatToQuestion.cheated = state.cheated;
        mediator.setCheatToQuestionState(cheatToQuestion);

        // Volver a Question
        view.get().finishView();
    }

    @Override
    public void onWarningButtonClicked(int option) {
        Log.e(TAG, "onWarningButtonClicked");
        // option=1 => Yes, option=0 => No

        if (option == 1) {
            // Pulsó Yes: mostrar respuesta y desactivar botones
            state.answer = model.getCorrectAnswer();
            state.buttonEnabled = false;
            state.cheated = true;

        } else {
            // Pulsó No: volver sin haber visto la respuesta
            CheatToQuestionState cheatToQuestion = new CheatToQuestionState();
            cheatToQuestion.cheated = false;
            mediator.setCheatToQuestionState(cheatToQuestion);
            view.get().finishView();
        }

        view.get().displayAnswerData(state);
    }

    @Override
    public void injectView(WeakReference<CheatContract.View> view) {
        this.view = view;
    }

    @Override
    public void injectModel(CheatContract.Model model) {
        this.model = model;
    }
}