package com.example.coroutinestudy

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    fun coroutine_1(view: View) {
        //runblocking은 내부 로직이 끝날도록 외부에게 기다리게 하는데, 코로틴은 비동기적으로 돌아가는 방식이라
        //서로 반대되는 방식이기 때문에 아래 코드가 정상적으로 돌아가는 것을 보장할 수 없다.

        runBlocking {
            println("start")
            /** A Scope */
            CoroutineScope(context = Dispatchers.IO).launch {
                Log.d("MainActivity", "testCoruotine - A Scope : I'am CoroutineScope, start!")
                for (item in 0..10) {
                    Log.d("MainActivity", "coroutine_1 - A Scope : $item")
                }
            }
            /** B Scope */
            CoroutineScope(Dispatchers.IO).launch {
                Log.d("MainActivity", "testCoruotine - B Scope : I'am CoroutineScope, start!")
                for (item in 0..10) {
                    Log.d("MainActivity", "coroutine_1 - B Scope : $item")
                }
            }
        }
    }

    fun coroutine_2(view: View) {
        //coroutine_1 과 같은 방식으로 반대 기능을 하는 방식이지만 async를 사용한 A scope는 작업이 보장이 된다. B는 안됨
        //A,A-2는 동시 출력 , B는 동시 출력x
        runBlocking {
            println("start")
            /** A Scope */
            val job = CoroutineScope(context = Dispatchers.IO).async {
                Log.d("MainActivity", "testCoruotine - A Scope : I'am CoroutineScope, start!")
                for (item in 0..10) {
                    Log.d("MainActivity", "coroutine_1 - A Scope : $item")
                }
            }
            CoroutineScope(context = Dispatchers.IO).async {
                Log.d("MainActivity", "testCoruotine - A_2 Scope : I'am CoroutineScope, start!")
                for (item in 0..10) {
                    Log.d("MainActivity", "coroutine_1 - A_2 Scope : $item")
                }
            }.await()   //해당 스레드가 종료 될 때까지 기다림 끝나면 B실행
            /** B Scope */
            CoroutineScope(Dispatchers.IO).launch {
                Log.d("MainActivity", "testCoruotine - B Scope : I'am CoroutineScope, start!")
                for (item in 0..10) {
                    Log.d("MainActivity", "coroutine_1 - B Scope : $item")
                }
            }
            job.await() //결과 값을 반환 받아 로그에 출력

        }
    }

    fun coroutine_3(view: View) {
        //A는 출력 보장, B는 x
        //withContext는 coroutineContext의 변경일뿐
        //CoroutineContext를 실행 인자와 suspend 함수를 인자로 받고 그 CoroutineContext에서 그 suspend 함수를 실행
        //그리고 부모 CoroutineContext는 이 전체 실행이 끝나고 결과를 반환할 때까지 기다림

        runBlocking {
            println("start")
            /** A Scope */
            withContext(context = Dispatchers.IO) {
                Log.d("MainActivity", "testCoruotine - A Scope : I'am CoroutineScope, start!")
                for (item in 0..10) {
                    Log.d("MainActivity", "coroutine_1 - A Scope : $item")
                }
            }
            /** B Scope */
            CoroutineScope(Dispatchers.IO).launch {
                Log.d("MainActivity", "testCoruotine - B Scope : I'am CoroutineScope, start!")
                for (item in 0..10) {
                    Log.d("MainActivity", "coroutine_1 - B Scope : $item")
                }
            }
        }
    }

    fun Flow_1(view: View) {

        //1. Flow 빌더
        //가장 기본적인 플로우 빌더
        //emit() 이외에 asFlow()를 통하여 Collection 및 Sequence를 Flow로 변환 할 수 있습니다.
        CoroutineScope(Dispatchers.IO).launch { //코루틴 없으면 suspend해야함
            (1..10).asFlow().collect { value ->
                Log.d("MainActivity", "Flow_1 - asFlow() : $value")
            }
        }

        //3.Intermediary(중간 연산자)
        //생산자에서 데이터를 발행을 하였다면 중간 연산자는 생성된 데이터를 수정 할 수 있습니다 .
        //코틀린의 컬렉션의 함수와 같이 대표적으로 map(데이터를 원하는 형태로 변환), filter(데이터 필터링), onEach(데이터를 변경후 수행한 결과를 반환) 등이 있습니다.
        /*
        flowSomething().filter {
            it % 2 == 0 // 짝수만 필터링
        }

        flowSomething().map {
            it * 2 // 값을 2배씩
        }*/


        //4.Consumer(소비자)
        //생산자에서 데이터를 발행하고, 중간 연산자(선택)에서 데이터를 가공하였다면 소비자에서는 collect()를 이용하여 전달된 데이터를 소비할 수 있습니다.
        runBlocking {
            flowSomething().map {
                it * 2
            }.collect { value ->
                Log.d("MainActivity", "Flow_1 - flow collect1 : $value")
            }
        }

        runBlocking {
            flowSomething().collect { value ->
                Log.d("MainActivity", "Flow_1 - flow collect2 : $value")
            }
        }

    }

    //2.Producer(생산자)
    //먼저 생산자에서는 데이터를 발행하기 위하여 flow {} 코루틴 블록(빌더) 을 생성한 후 내부에서 emit () 을 통하여 데이터를 생성합니다.또한 flow {} 블록은 suspend 함수이므로 delay를 호출할 수 있습니다 .
    private fun flowSomething(): Flow<Int> = flow {
        repeat(10) {
            emit(it) // 0 1 2 3..9
            delay(100L) // 100ms
        }
    }

    fun Flow_2(view: View) {
        //Flow 취소 withTimeoutOrNull
        runBlocking<Unit> {
            val result = withTimeoutOrNull(500L) {
                flowSomething().collect { value ->
                    println(value)
                }
                true
            } ?: false
            if (!result) {
                println("취소되었습니다.")
            }
        }

    }

    fun Flow_3(view: View) {

        println("flowOf()")
        runBlocking<Unit> {
            flowOf(1, 2, 3, 4, 5).collect { value ->
                println(value)
            }
        }

        println("listOf().asFolw()")
        runBlocking<Unit> {
            listOf(1, 2, 3, 4, 5).asFlow().collect { value ->
                println(value)
            }
            (6..10).asFlow().collect {
                println(it)
            }
        }


    }

    fun Flow_4(view: View) {
        //데이터 가공

        println("Flow + map")
        runBlocking {
            flowSomething().map {
                "$it $it"
            }.collect { value ->
                println(value)
            }
        }

        println("Flow + filter")
        runBlocking<Unit> {
            (1..20).asFlow().filter {
                (it % 2) == 0 //짝수만 필터링
            }.collect {
                println(it)
            }
        }

        println("Flow + transform")
        runBlocking<Unit> {
            (1..10).asFlow().transform {
                emit("cal($it): ${someCalc(it)}")
            }.collect {
                println(it)
            }
        }

        println("Flow + transform + take")
        runBlocking<Unit> {
            (1..20).asFlow().transform {
                emit("cal($it): ${someCalc(it)}")
            }.take(5)   //일부 수행 결과만 취함.
                .collect {
                    println(it)
                }
        }

        println("Flow + transform + takeWhile")
        runBlocking<Unit> {
            (1..20).asFlow().transform {
                emit(someCalc(it))
            }.takeWhile {   //조건을 만족하면
                it < 15     //15보다 작은 결과만 취해서
            }.collect {
                println(it)     //보여줌
            }
        }


        println("Flow + transform + drop")
        runBlocking<Unit> {
            (1..10).asFlow().transform {
                emit("cal($it): ${someCalc(it)}")
            }.drop(5)   //처음 부터 일부의 결과를 버리고 나머지만 취함
                .collect {
                    println(it)
                }
        }

    }

    suspend fun someCalc(i: Int): Int {
        delay(10L)
        return i * 3
    }
}