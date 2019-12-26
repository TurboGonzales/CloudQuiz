package by.bsuir.cloud_quiz_app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import by.bsuir.cloud_quiz_app.R;
import by.bsuir.cloud_quiz_app.dto.TestResult;

public class ResultActivity
        extends AppCompatActivity
        implements FirebaseAuth.AuthStateListener {

    private TextView tvResult;
    private TextView tvWrongCount;
    private TextView tvUnansweredCount;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        tvResult = findViewById(R.id.tv_result);
        tvWrongCount = findViewById(R.id.tv_wrong_count);
        tvUnansweredCount = findViewById(R.id.tv_unanswered_count);

        mAuth = FirebaseAuth.getInstance();
        mAuth.addAuthStateListener(this);

        db = FirebaseFirestore.getInstance();

        checkTestAndShowResult();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_items, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_records:
                startActivity(new Intent(this, RecordsActivity.class));
                finish();
                break;
            case R.id.item_tests:
                startActivity(new Intent(this, SelectTestActivity.class));
                finish();
                break;
            case R.id.item_exit:
                mAuth.signOut();
                break;
        }
        return true;
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    private void checkTestAndShowResult() {
        TestResult testResult = (TestResult) getIntent()
                .getSerializableExtra("TEST_RESULT");
        int correctAnswersCount = 0;
        int wrongCount = 0;
        int unansweredCount = 0;
        int questionsCount = testResult.getQuestionList().size();

        for (int i = 0; i < questionsCount; i++) {
            int correctAnswerIndex = testResult.getQuestionList().get(i)
                    .getCorrectAnswerIndex();
            int userAnswerIndex = testResult.getUserAnswersIndexesList().get(i);
            if (userAnswerIndex == -1) {
                unansweredCount++;
            } else if (correctAnswerIndex != userAnswerIndex) {
                wrongCount++;
            } else {
                correctAnswersCount++;
            }
        }

        Integer correctAnswersPercent = Math.round((float) correctAnswersCount / questionsCount * 100);
        tvResult.setText("Правильно отвечено на "
                + correctAnswersCount
                + " из " + questionsCount + " вопросов (" + correctAnswersPercent + "%)");
        tvWrongCount.setText("Неправильных ответов: " + wrongCount);
        tvUnansweredCount.setText("Оставлено без ответа: " + unansweredCount);

        testResult.setCorrectAnswersPercent(correctAnswersPercent);

        uploadResultsToServer(testResult);
    }

    private void uploadResultsToServer(TestResult testResult) {
        db.collection("results")
                .add(testResult)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Toast.makeText(
                                this,
                                R.string.data_load_failed,
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
