package com.example.coroutinestudy

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import kotlinx.coroutines.*

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
        runBlocking {
            println("start")
            /** A Scope */
            val job = CoroutineScope(context = Dispatchers.IO).async {
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
            job.await() //결과 값을 반환 받아 로그에 출력
            
        }
    }


}