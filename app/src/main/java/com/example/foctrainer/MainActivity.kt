package com.example.foctrainer

import android.app.PendingIntent.getActivity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.foctrainer.exercise.Exercise
import com.example.foctrainer.databinding.ActivityMainBinding
import android.graphics.Color
import android.widget.*
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foctrainer.databaseConfig.FocTrainerApplication
import com.example.foctrainer.entity.CompletedExerciseModel
import com.example.foctrainer.entity.ExerciseModel
import com.example.foctrainer.entity.UserModel
import com.example.foctrainer.fragments.HomeFragment
import com.example.foctrainer.fragments.ScheduleFragment
import com.example.foctrainer.viewModel.*
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.bottomnavigation.BottomNavigationView

import android.widget.TextView
import com.github.mikephil.charting.data.*
import com.google.android.material.navigation.NavigationView


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var lineValuesList: ArrayList<Entry>
    //Line Chart
    private lateinit var lineDataSet: LineDataSet
    private lateinit var dataLine: LineData
    private val exList = ArrayList<ExerciseModel>()
    private lateinit var exerciseAdapter: RecyclerAdapter
    //BarChart
    private lateinit var myBarChart: BarChart

    val homeFragment = HomeFragment()
    val scheduleFragment = ScheduleFragment()

    var userID = 0
    var exerciseName = ""

    private val exerciseViewModel: ExerciseViewModel by viewModels {
        ExerciseViewModelFactory((application as FocTrainerApplication).exerciseRepository)
    }

    private val userViewModel: UserViewModel by viewModels {
        UserViewModelFactory((application as FocTrainerApplication).userRepository)
    }

    private val completeExerciseModel: CompletedExerciseViewModel by viewModels {
        CompletedExerciseViewModelFactory((application as FocTrainerApplication).completedExerciseRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//        setContentView(R.layout.activity_main)
        setContentView(R.layout.activity_main)

        //Navigation Bar
        makeCurrentFragment(homeFragment)
        val bottomNavView = findViewById<BottomNavigationView>(R.id.bottom_navigation_bar)
        bottomNavView.setOnNavigationItemSelectedListener{ it ->
            when(it.itemId){
                R.id.home -> makeCurrentFragment(homeFragment)
                R.id.schedule -> makeCurrentFragment(scheduleFragment)
            }
            true
        }

        //database testing: data insertion
        val user = UserModel(4,"jiayi","123",155.3f,45f,18.9f)
        userViewModel.createNewUser(user = user)

        userViewModel.allUsers.observe(this, Observer{ users ->
            Log.d("db insertion testing", users[3].userName)
        })

//        completeExerciseModel.chartSummary.observe(this, Observer { ex->
//            Log.d("Chart", ex.toString())
//        })

        val bundle = Bundle()
        userViewModel.allUsers.observe(this, Observer<List<UserModel>>() { users ->
            bundle.putString("name", "${users[0].userName}")
            bundle.putString("height", "${users[0].height}")
            bundle.putString("weight", "${users[0].weight}")
            bundle.putString("bmi", "${users[0].bmi}")

            val myObj = HomeFragment()
            myObj.setArguments(bundle)
            Log.d("obj", myObj.arguments.toString())
        })


//        val recyclerView: RecyclerView = findViewById(R.id.recyclerview)
//        exerciseAdapter = RecyclerAdapter(exList)
//        val layoutManager = LinearLayoutManager(applicationContext)
//        recyclerView.layoutManager = layoutManager
//        recyclerView.itemAnimator = DefaultItemAnimator()
//        recyclerView.adapter = exerciseAdapter

//        displayUserDetails()
//        exerciseDetails()
//        setLineChartData()
        //Barchart
        //myBarChart = findViewById(R.id.BarChart)
        //populateBarChart()
    }
    private fun makeCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.wrapper, fragment)
            commit()
        }
    fun selectButtonOnClick(view: View){
        val intent = Intent(this, Exercise::class.java)
        startActivity(intent)
    }

    fun displayUserDetails() {
        val f1: FrameLayout? =
            supportFragmentManager.findFragmentById(R.id.root_container) as FrameLayout?
        val tvName = f1?.findViewById<TextView>(R.id.name)
        val textEditHeight = f1?.findViewById<EditText>(R.id.editHeight)
        val textEditWeight = f1?.findViewById<EditText>(R.id.editWeight)
        val tvBmi = f1?.findViewById<TextView>(R.id.bmi)
        Log.d("WHY", tvName.toString())

        tvName?.text = ""
        tvBmi?.text = ""

        userViewModel.allUsers.observe(this, Observer<List<UserModel>>() { users ->
            userID = users[0].id
            tvName?.append(users[0].userName)
            textEditHeight?.setText(users[0].height.toString())
            textEditWeight?.setText(users[0].weight.toString())
            tvBmi?.append(users[0].bmi.toString())
        })
    }


//    fun exerciseDetails(){
        //Exercise
//        val exlist = ArrayList<String>()
//        val exerciseListView = findViewById<ListView>(R.id.workoutCategoriesListView)
//
//        //get recycle view
//        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)
//        recyclerView.layoutManager = LinearLayoutManager(this)
//
//        exerciseViewModel.allExercise.observe(this, Observer<List<ExerciseModel>>(){ exercise->
//            for (ex in exercise){
//                exerciseName = ex.name
////                exerciseID = ex.id
//                exlist.add("$exerciseName")
//            }
//            //attach the array adapter to the list view
//            exerciseListView.adapter = ArrayAdapter<String>(
//                this, android.R.layout.simple_list_item_1,exlist)
//
////            recyclerView.adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, exlist)
//        })
//        exerciseListView.setOnItemClickListener { adapterView, view, i, l ->
//            Toast.makeText(this, "You have selected " + exlist[i], Toast.LENGTH_LONG).show()
//        }
//
//        var totalCalories = 0f
//        val tvTotalCalories = findViewById<TextView>(R.id.totalCalories)
//        tvTotalCalories.text = ""
//        completeExerciseModel.completedExercise.observe(this, Observer<List<CompletedExerciseModel>>(){ completedExercise->
//            for ( comEx in completedExercise){
//                val exUserId = comEx.userId
//                if (exUserId == userID){
//                    totalCalories += comEx.total_calories
//                }
//            }
//            tvTotalCalories.append(totalCalories.toString())
//        })
//    }

    //line Chart
//    fun setLineChartData(){
//        val lineChart = findViewById<LineChart>(R.id.lineChart)
//        lineValuesList = ArrayList();
//        completeExerciseModel.completedExercise.observe(this, Observer<List<CompletedExerciseModel>>(){ completedExercise->
//            for (eachEx in completedExercise){
//                val comExUserId = eachEx.userId
//                val comExExerciseId = eachEx.exerciseId
//                val calories = eachEx.total_calories
//                if (comExUserId == userID){
//                    lineValuesList.add(Entry(comExExerciseId.toFloat(), calories))
//                }
//            }
//            lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM)
//            lineChart.axisRight.isEnabled = false
//            lineDataSet = LineDataSet(lineValuesList, "WorkOut")
//            dataLine = LineData(lineDataSet)
//            lineChart.data = dataLine
//
//            lineDataSet.setColors(*ColorTemplate.MATERIAL_COLORS)
//            lineDataSet.valueTextColor = Color.BLACK
//            lineDataSet.valueTextSize = 15f
//            lineDataSet.setDrawFilled(true)
//
//            lineChart.setBackgroundColor(resources.getColor(R.color.white))
//            lineChart.animateXY(2000,2000)
//        })
//
//    }

    //Barchart
//    fun populateBarChart() {
//        //adding values
//        val myBarEntries: ArrayList<BarEntry> = ArrayList()
//        var i = 0
//
//        completeExerciseModel.chartSummary.observe(this, Observer<List<String>>() { completedExercise ->
//            for (eachEx in completedExercise) {
//                Log.d("PLS", eachEx.toString())
//                val comExUserId = eachEx.userId
//                val comExExerciseId = eachEx.exerciseId
//                val calories = eachEx.total_calories
//                if (comExUserId == userID) {
//                    myBarEntries.add(BarEntry(comExExerciseId.toFloat(), calories))
//                }
//            }
//
//            val barDataSet = BarDataSet(myBarEntries, "Work Out Summary")
//            //set a template coloring
//            barDataSet.setColors(*ColorTemplate.COLORFUL_COLORS)
//            val data = BarData(barDataSet)
//            myBarChart.data = data
//            //setting the x-axis
//            val xAxis: XAxis = myBarChart.xAxis
//            //calling methods to hide x-axis gridlines
//            myBarChart.axisLeft.setDrawGridLines(false)
//            xAxis.setDrawGridLines(false)
//            //xAxis.setDrawAxisLine(false)
//            myBarChart.setBackgroundColor(resources.getColor(R.color.white))
//            //remove legend
//            myBarChart.legend.isEnabled = true
//
//            //remove description label
//            myBarChart.description.isEnabled = false
//
//            //add animation
//            myBarChart.animateY(3000)
//            //refresh the chart
//            myBarChart.invalidate()
//        })
//    }

}