package com.ivianuu.list.sample

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.ivianuu.list.annotations.Model
import com.ivianuu.list.common.LayoutContainerHolder
import com.ivianuu.list.common.LayoutContainerModel
import com.ivianuu.list.common.controller
import com.ivianuu.list.common.onClick
import com.ivianuu.list.id
import kotlinx.android.synthetic.main.activity_main.list
import kotlinx.android.synthetic.main.item_simple.title

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        list.layoutManager = LinearLayoutManager(this)

        val controller = controller {
            (1..100)
                .map { i ->
                    simple {
                        id(i)
                        text("Title: $i")
                        onClick { _, _ ->
                            Toast.makeText(
                                this@MainActivity, "Clicked model at $i",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
        }

        controller.requestModelBuild()

        list.adapter = controller.adapter
    }
}

@Model class SimpleModel : LayoutContainerModel() {
    var text by requiredProperty<String>("text")
    override val layoutRes = R.layout.item_simple
    override fun onBind(holder: LayoutContainerHolder) {
        super.onBind(holder)
        holder.title.text = text
    }
}
