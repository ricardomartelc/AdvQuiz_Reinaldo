package es.ulpgc.eite.da.advquiz.question;

import android.util.Log;

import java.lang.ref.WeakReference;

import es.ulpgc.eite.da.advquiz.app.AppMediator;
import es.ulpgc.eite.da.advquiz.app.CheatToQuestionState;
import es.ulpgc.eite.da.advquiz.app.QuestionToCheatState;

public class QuestionPresenter implements QuestionContract.Presenter {

    public static String TAG = "AdvQuiz.QuestionPresenter";

    private AppMediator mediator;
    private WeakReference<QuestionContract.View> view;
    private QuestionState state;   // Para guardar por si se gira la pantalla.
    private QuestionContract.Model model;

    public QuestionPresenter(AppMediator mediator) {
        this.mediator = mediator;
    }

    @Override
    public void onCreatedCalled() {
        Log.e(TAG, "onCreatedCalled");

        // Inicializar estado desde cero
        state = new QuestionState();
        state.quizIndex = model.getQuizIndex(); // 0

        // Cargar primera pregunta
        loadCurrentQuestion();
    }

    @Override
    public void onRecreatedCalled() {
        Log.e(TAG, "onRecreatedCalled");

        // Restaurar estado guardado tras rotación
        state = mediator.getQuestionState();
    }

    @Override
    public void onResumeCalled() {
        Log.e(TAG, "onResumeCalled");

        // Abre el sobre que Cheat dejó en el mediator (se auto-nullifica al leerlo)
        CheatToQuestionState savedState = mediator.getCheatToQuestionState();

        // Si es null: primera vez, rotación o volvimos con No → no hacer nada
        if (savedState != null) {

            // Si el usuario vio la respuesta en Cheat
            if (savedState.cheated) {

                // Si quedan más preguntas → avanzar a la siguiente
                if (!model.hasQuizFinished()) {
                    model.incrQuizIndex();                  // avanza índice en el modelo (+5)
                    state.quizIndex = model.getQuizIndex(); // sincroniza índice en el state
                    loadCurrentQuestion();                  // carga datos de la nueva pregunta

                } else {
                    // Era la última pregunta → bloquear todo
                    state.optionEnabled = false;
                    state.nextEnabled = false;
                    state.cheatEnabled = true;   /////DEBE ESTAR ACTIVO
                }
            }
            // Si cheated=false (pulsó No) → no tocamos nada, state ya tiene lo correcto
        }

        // Siempre pintamos la vista, sea cual sea el caso
        view.get().displayQuestionData(state);
    }

    @Override
    public void onPauseCalled() {
        Log.e(TAG, "onPauseCalled");

        // Guardar estado para sobrevivir rotaciones y navegación a Cheat
        mediator.setQuestionState(state);
    }

    @Override
    public void onDestroyCalled() {
        Log.e(TAG, "onDestroyCalled");
    }

    @Override
    public void onOptionButtonClicked(int option) {
        Log.e(TAG, "onOptionButtonClicked");

        boolean isCorrect = model.isCorrectOption(option);

        state.optionEnabled = false;                   // siempre se desactiva
        state.nextEnabled = !model.hasQuizFinished();  // activo si no es última pregunta

        if (isCorrect) {
            state.result = model.getCorrectResultText();
            state.cheatEnabled = false;                // correcto: no necesita trampa
        } else {
            state.result = model.getIncorrectResultText();
            state.cheatEnabled = true;                 // incorrecto: puede hacer trampa
        }

        view.get().displayQuestionData(state);
    }

    @Override
    public void onNextButtonClicked() {
        Log.e(TAG, "onNextButtonClicked");

        // Avanzar a siguiente pregunta
        model.incrQuizIndex();
        state.quizIndex = model.getQuizIndex();
        loadCurrentQuestion();

        view.get().displayQuestionData(state);
    }

    @Override
    public void onCheatButtonClicked() {
        Log.e(TAG, "onCheatButtonClicked");

        // Empaquetar respuesta correcta y enviarla a Cheat
        QuestionToCheatState data = new QuestionToCheatState();
        data.correctAnswer = model.getCorrectAnswer();
        mediator.setQuestionToCheatState(data);

        view.get().navigateToCheatScreen();
    }

    // Carga la pregunta actual del modelo en el estado
    private void loadCurrentQuestion() {
        state.question = model.getQuestion();
        state.option1 = model.getOption1();
        state.option2 = model.getOption2();
        state.option3 = model.getOption3();
        state.result = model.getEmptyResultText();
        state.optionEnabled = true;
        state.nextEnabled = false;
        state.cheatEnabled = true;
    }

    @Override
    public void injectView(WeakReference<QuestionContract.View> view) {
        this.view = view;
    }

    @Override
    public void injectModel(QuestionContract.Model model) {
        this.model = model;
    }
}

