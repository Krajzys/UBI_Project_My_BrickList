package com.ubiquitous.bricklist

import android.app.ActionBar
import android.app.Activity
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.marginBottom
import kotlinx.android.synthetic.main.activity_view_project.*

class ViewProjectActivity : AppCompatActivity() {
    // TODO: Maybe sorting ?
    // TODO: Make it possible to save project to XML
    var projectCodeView = 0
    var projectNameView = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_project)
        val extras = intent.extras?: return

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "View Project " + extras.get("projectcode") + " " + extras.get("projectname")
        projectCodeView = extras.get("projectcode").toString().toInt()
        projectNameView = extras.get("projectname").toString()

        refreshView()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // TODO: Download Images
    fun refreshView() {
        viewProjectLayout.removeAllViews()

        val DBHelp = DBHelperino(this)
        val myDB = DBHelp.readableDatabase

        val query = "SELECT * FROM inventoriesparts WHERE inventoryid=$projectCodeView"
        val cursor = myDB.rawQuery(query, null)
        var count = 0

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast) {
                val row = TableRow(this)
                val linLayV = LinearLayout(this)
                linLayV.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT)
                linLayV.orientation = LinearLayout.VERTICAL

                val linLayH1 = LinearLayout(this)
                linLayH1.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT)
                linLayH1.orientation = LinearLayout.HORIZONTAL

                val linLayH2 = LinearLayout(this)
                linLayH2.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT)
                linLayH2.orientation = LinearLayout.HORIZONTAL

                val itemid = cursor.getInt(3)
                val quanitity = cursor.getInt(4)
                val itemname = DBHelp.findPartByID(itemid)?.name
                val quantityHave = cursor.getInt(5)
                val itemcode = DBHelp.findPartByID(itemid)?.code
                val image = ImageView(this)

                //row.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT)
                val params = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT)
                params.setMargins(0,0,0,40)
                row.layoutParams = params
                row.setBackgroundColor(android.graphics.Color.LTGRAY)

                image.setImageDrawable(resources.getDrawable(R.drawable.ic_launcher_foreground))
                image.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT)
                linLayH1.addView(image)

                val text = TextView(this)
                text.setText(itemname + "\n" + itemcode + "\n" + quantityHave + "/" + quanitity.toString())
                text.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.MATCH_PARENT)
                linLayH1.addView(text)

                val plusBtn = Button(this)
                //plusBtn.setBackgroundResource(R.drawable.ic_add_circle_black_24dp)
                plusBtn.setTag(cursor.getInt(0))
                plusBtn.setText("PLUS")
                plusBtn.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1.0f)
                plusBtn.setOnClickListener {
                    var inventoryPart = DBHelp.findInventoryPartByID(it.tag as Int)
                    if (inventoryPart != null) {
                        if (inventoryPart.quantityInStore < inventoryPart.quantityInSet) {
                            inventoryPart.quantityInStore += 1
                            DBHelp.editInventoryPart(inventoryPart)
                        }
                    }
                    refreshView()
                }
                linLayH2.addView(plusBtn)

                val minusBtn = Button(this)
                //minusBtn.setBackgroundResource(R.drawable.ic_remove_circle_black_24dp)
                minusBtn.setText("MINUS")
                minusBtn.setTag(cursor.getInt(0))
                minusBtn.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1.0f)
                minusBtn.setOnClickListener {
                    var inventoryPart = DBHelp.findInventoryPartByID(it.tag as Int)
                    if (inventoryPart != null) {
                        if (inventoryPart.quantityInStore > 0) {
                            inventoryPart.quantityInStore -= 1
                            DBHelp.editInventoryPart(inventoryPart)
                        }
                    }
                    refreshView()
                }
                linLayH2.addView(minusBtn)

                linLayV.addView(linLayH1)
                linLayV.addView(linLayH2)

                row.addView(linLayV)

                viewProjectLayout.addView(row, count)
                count += 1
                cursor.moveToNext()
            }
        }

        cursor.close()
        myDB.close()

    }

    fun toastMessage(content: String, isLong: Boolean) {
        var length = if (isLong) {
            Toast.LENGTH_LONG
        } else {
            Toast.LENGTH_SHORT
        }
        Toast.makeText(this, content, length).show()
    }
}
