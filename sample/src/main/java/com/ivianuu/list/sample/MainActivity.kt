package com.ivianuu.list.sample

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.ivianuu.list.addModelListener
import com.ivianuu.list.common.ModelTouchHelper
import com.ivianuu.list.common.modelController
import com.ivianuu.list.common.onClick
import com.ivianuu.list.id
import kotlinx.android.synthetic.main.activity_main.list
import java.util.*

class MainActivity : AppCompatActivity() {

    private val models = mutableListOf<String>()

    private var countModelCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        models.addAll((1..100).map { "Title: $it" })

        list.layoutManager = LinearLayoutManager(this)

        val controller = modelController {
            ButtonModel {
                id("shuffle")
                buttonText = "Shuffle"
                onClick(R.id.button) { _, _ ->
                    models.shuffle()
                    requestModelBuild()
                }
            }

            CountModel {
                id("count")
                count = countModelCount
                onClick(R.id.inc_button) { _, _ ->
                    countModelCount++
                    requestModelBuild()
                }
                onClick(R.id.dec_button) { _, _ ->
                    countModelCount--
                    requestModelBuild()
                }
            }

            ButtonModel {
                id("add_random")
                buttonText = "Add Random"
                onClick(R.id.button) { _, _ ->
                    models.add(0, "Random ${UUID.randomUUID()}")
                    requestModelBuild()
                }
            }

            models.forEach {
                SimpleModel {
                    id(it)
                    text = it
                    onClick { _, _ ->
                        Toast.makeText(
                            this@MainActivity, "Clicked model at $it",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        controller.adapter.addModelListener(
            preBind = { model, holder ->
                println("pre bind $model $holder view type is ${model.viewType}")
            },
            postUnbind = { model, holder ->
                println("post unbind $model $holder view type is ${model.viewType}")
            }
        )

        list.adapter = controller.adapter

        ModelTouchHelper.dragging(list)
            .vertical()
            .target(SimpleModel::class)
            .callbacks(
                object : ModelTouchHelper.DragCallbacks<SimpleModel>() {
                    override fun onModelMoved(
                        fromPosition: Int,
                        toPosition: Int,
                        modelBeingMoved: SimpleModel,
                        itemView: View
                    ) {
                        super.onModelMoved(fromPosition, toPosition, modelBeingMoved, itemView)
                        Collections.swap(models, fromPosition - 3, toPosition - 3)
                        controller.requestDelayedModelBuild(500)
                    }
                }
            )

        ModelTouchHelper.swiping(list)
            .leftAndRight()
            .target(SimpleModel::class)
            .callbacks(
                object : ModelTouchHelper.SwipeCallbacks<SimpleModel>() {
                    override fun onSwipeCompleted(
                        model: SimpleModel,
                        itemView: View,
                        position: Int,
                        direction: Int
                    ) {
                        super.onSwipeCompleted(model, itemView, position, direction)
                        models.removeAt(position - 3)
                        controller.requestModelBuild()
                    }
                }
            )

        controller.requestModelBuild()
    }
}