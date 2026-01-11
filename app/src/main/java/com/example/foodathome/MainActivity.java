package com.example.foodathome;

import static com.example.foodathome.FirebaseDataHandler.ingredientSem;
import static autovalue.shaded.com.google.common.collect.ComparisonChain.start;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final Logger log = LoggerFactory.getLogger(MainActivity.class);
    EditText dishET;
    Button searchBT;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dishET = findViewById(R.id.dishET);
        searchBT = findViewById(R.id.searchBT);

        searchBT.setOnClickListener(this);
    }


    @Override
    public void onClick(View view)
    {
        if(view == searchBT)
        {
            /*
            String dish = dishET.getText().toString();
            if(!dish.isEmpty())
            {
                Intent intent = new Intent(this, recipeActivity.class);
                intent.putExtra("DISH",dish );
                startActivity(intent);
            }
            else
            {
                Toast.makeText(this, "dist must contain a value", Toast.LENGTH_SHORT).show();
            }*/

                Log.i("myComments", "getting ingredients");
                //FirebaseDataHandler.getIngredients(s,true);
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("Ingredients")
                        .get();

//                a.addOnSuccessListener((QuerySnapshot qs) -> {
//                }).addOnFailureListener((Exception e) -> {
//                    Log.i("myComments", "listner failure" + e.toString());
//                });

//                Log.i("myComments", "waiting");

        }
    }

    void temp(List<Ingredient> s, DocumentSnapshot doc) {
        s.get(0).copy(doc.toObject(Ingredient.class));
        s.get(0).setId(doc.getId());
    }
}

