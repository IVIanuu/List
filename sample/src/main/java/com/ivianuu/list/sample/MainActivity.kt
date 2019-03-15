package com.ivianuu.list.sample

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ivianuu.list.ListModel
import com.ivianuu.list.annotations.Model
import com.ivianuu.list.common.LayoutContainerHolder
import com.ivianuu.list.common.LayoutContainerModel
import com.ivianuu.list.common.ModelTouchHelper
import com.ivianuu.list.common.modelController
import com.ivianuu.list.common.onClick
import com.ivianuu.list.id
import com.ivianuu.list.moveModel
import com.ivianuu.list.removeModel
import kotlinx.android.synthetic.main.activity_main.list
import kotlinx.android.synthetic.main.item_button.button
import kotlinx.android.synthetic.main.item_count.count
import kotlinx.android.synthetic.main.item_simple.title
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
            button {
                id("shuffle")
                buttonText("Shuffle")
                onClick(R.id.button) { _, _ ->
                    models.shuffle()
                    requestModelBuild()
                }
            }

            count {
                id("count")
                count(countModelCount)
                onClick(R.id.inc_button) { _, _ ->
                    countModelCount++
                    requestModelBuild()
                }
                onClick(R.id.dec_button) { _, _ ->
                    countModelCount--
                    requestModelBuild()
                }
            }

            button {
                id("add_random")
                buttonText("Add Random")
                onClick(R.id.button) { _, _ ->
                    models.add(0, "Random ${UUID.randomUUID()}")
                    requestModelBuild()
                }
            }

            models.forEach {
                simple {
                    id(it)
                    text(it)
                    onClick { _, _ ->
                        Toast.makeText(
                            this@MainActivity, "Clicked model at $it",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        list.adapter = controller.adapter

        controller.adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                println("on changed")
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                println("on item range changed position start: $positionStart, item count: $itemCount")
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                println("on item range inserted position start: $positionStart, item count: $itemCount")
            }

            override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
                println("on item range moved from position: $fromPosition, to position: $toPosition, item count: $itemCount")
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                println("on item range removed position start: $positionStart, item count: $itemCount")
            }
        })

        ModelTouchHelper.dragging(list)
            .vertical()
            .all()
            .callbacks(
                object : ModelTouchHelper.DragCallbacks<ListModel<*>>() {
                    override fun onModelMoved(
                        fromPosition: Int,
                        toPosition: Int,
                        modelBeingMoved: ListModel<*>,
                        itemView: View
                    ) {
                        super.onModelMoved(fromPosition, toPosition, modelBeingMoved, itemView)
                        Collections.swap(models, fromPosition, toPosition)
                        controller.adapter.moveModel(fromPosition, toPosition)
                    }
                }
            )

        ModelTouchHelper.swiping(list)
            .leftAndRight()
            .all()
            .callbacks(
                object : ModelTouchHelper.SwipeCallbacks<ListModel<*>>() {
                    override fun onSwipeCompleted(
                        model: ListModel<*>,
                        itemView: View,
                        position: Int,
                        direction: Int
                    ) {
                        super.onSwipeCompleted(model, itemView, position, direction)
                        models.removeAt(position)
                        controller.adapter.removeModel(model)
                    }
                }
            )

        controller.requestModelBuild()
    }
}

@Model class ButtonModel : LayoutContainerModel() {
    var buttonText by requiredProperty<String>("buttonText")
    override val layoutRes = R.layout.item_button
    override fun bind(holder: LayoutContainerHolder) {
        super.bind(holder)
        holder.button.text = buttonText
    }
}

@Model class CountModel : LayoutContainerModel() {
    var count by requiredProperty<Int>("count")
    override val layoutRes = R.layout.item_count
    override fun bind(holder: LayoutContainerHolder) {
        super.bind(holder)
        holder.count.text = "Count is $count"
    }
}

@Model class SimpleModel : LayoutContainerModel() {
    var text by requiredProperty<String>("text")
    override val layoutRes = R.layout.item_simple
    override fun bind(holder: LayoutContainerHolder) {
        super.bind(holder)
        holder.title.text = text
    }
}
