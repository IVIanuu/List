package com.ivianuu.list.sample

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.ivianuu.list.ListModel
import com.ivianuu.list.annotations.Model
import com.ivianuu.list.common.LayoutContainerHolder
import com.ivianuu.list.common.LayoutContainerModel
import com.ivianuu.list.common.ModelTouchHelper
import com.ivianuu.list.common.modelController
import com.ivianuu.list.common.onClick
import com.ivianuu.list.id
import com.ivianuu.list.moveModel
import kotlinx.android.synthetic.main.activity_main.list
import kotlinx.android.synthetic.main.item_simple.title
import java.util.*

class MainActivity : AppCompatActivity() {

    private val models = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        models.addAll((1..100).map { "Title: $it" })

        list.layoutManager = LinearLayoutManager(this)

        val controller = modelController {
            shuffle {
                id("shuffle")
                onClick(R.id.shuffle_button) { _, _ ->
                    models.shuffle()
                    requestImmediateModelBuild()
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

        controller.requestModelBuild()

        list.adapter = controller.adapter

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
    }
}

@Model class ShuffleModel : LayoutContainerModel() {
    override val layoutRes = R.layout.item_shuffle
}

@Model class SimpleModel : LayoutContainerModel() {
    var text by requiredProperty<String>("text")
    override val layoutRes = R.layout.item_simple
    override fun onBind(holder: LayoutContainerHolder) {
        super.onBind(holder)
        holder.title.text = text
    }
}
