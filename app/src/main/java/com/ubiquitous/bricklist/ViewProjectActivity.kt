package com.ubiquitous.bricklist

import android.R.attr.*
import android.app.ActionBar
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Picture
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.*
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_view_project.*
import org.w3c.dom.DOMImplementationSource
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.*
import java.lang.Exception
import java.net.MalformedURLException
import java.net.URL
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult


class ViewProjectActivity : AppCompatActivity() {
    var projectCodeView = 0
    var projectNameView = ""


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_project)
        val extras = intent.extras?: return
        val DBHelp = DBHelperino(this)

        floatingActionButton2.setOnClickListener {
            val inv = DBHelp.findInventoryByID(projectCodeView)
            if (inv != null) {
                writeXml(inv)
            }
        }

        DBHelp.close()

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

    private inner class ImageDownloader(val inventoryPart: InventoryPart, val context: Context, val viewImage: ImageView): AsyncTask<String, Int, String>() {

        var ourCode = ""

        override fun onPreExecute() {
            super.onPreExecute()
        }

        // Save image in DB
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            Log.i("Downloader", result)
            if (result == "success") { // Got it from lego.com
                viewImage.setImageDrawable(Drawable.createFromPath("$filesDir/images/" + ourCode))
                val params = viewImage.layoutParams
                params.width = 300
                params.height = 300
                viewImage.layoutParams = params
                val DBHelp = DBHelperino(context)
                val editCode = DBHelp.findCode(inventoryPart.itemID, inventoryPart.colorID)
                if (editCode != null) {
                    // open temp file and save to byte array
                    val file = File("$filesDir/images/" + ourCode)
                    var bytes = file.readBytes()
                    file.delete()
                    editCode.image = bytes
                    DBHelp.editCode(editCode)
                    DBHelp.close()
                }
            }
            else if (result == "success2") { // Got it from brick link
                viewImage.setImageDrawable(Drawable.createFromPath("$filesDir/images/" + ourCode + ".gif"))
                val params = viewImage.layoutParams
                params.width = 300
                params.height = 300
                viewImage.layoutParams = params
            }
            else if (result == "success3") { // Got it in database
                val DBHelp = DBHelperino(context)
                val bytes = DBHelp.findCode(inventoryPart.itemID, inventoryPart.colorID)?.image
                val readFile = File("$filesDir/images_temp/")
                if (!readFile.exists()) readFile.mkdirs()

                val fos = FileOutputStream("$readFile/temp" + inventoryPart.id)
                fos.write(bytes)

                viewImage.setImageDrawable(Drawable.createFromPath("$readFile/temp" + inventoryPart.id))
                val params = viewImage.layoutParams
                params.width = 300
                params.height = 300
                viewImage.layoutParams = params
                readFile.delete()
            }
        }

        override fun doInBackground(vararg p0: String?): String {
            val DBHelp = DBHelperino(context)
            val codeUpper = DBHelp.findCode(inventoryPart.itemID, inventoryPart.colorID)
            if (codeUpper != null) {
                if (codeUpper.image != null) {
                    DBHelp.close()
                    return "success3"
                }
            }
            val part = DBHelp.findPartByID(inventoryPart.itemID)
            var superCode: String? = ""
            if (part != null) {
                val code = DBHelp.findCode(inventoryPart.itemID, inventoryPart.colorID)
                superCode = code?.code.toString()
            }
            try {
                val url = URL("https://www.lego.com/service/bricks/5/2/" + superCode)
                val connection = url.openConnection()
                connection.connect()
                val lengthOfFile = connection.contentLength
                val isStream = url.openStream()
                val testDirectory = File("$filesDir/images")
                if (!testDirectory.exists()) testDirectory.mkdir()
                val fos = FileOutputStream("$testDirectory/$superCode")
                if (superCode != null)
                    ourCode = superCode
                val data = ByteArray(1024)
                var count = 0
                var total: Long = 0
                var progress = 0
                count = isStream.read(data)
                while (count != -1) {
                    total += count.toLong()
                    val progress_temp = total.toInt() * 100/ lengthOfFile
                    if (progress_temp % 10 == 0 && progress != progress_temp) {
                        progress = progress_temp
                    }
                    fos.write(data, 0, count)
                    count = isStream.read(data)
                }
                DBHelp.close()
                isStream.close()
                fos.close()
            } catch (e: MalformedURLException) {
                DBHelp.close()
                return "Malformed URL"
            } catch (e: FileNotFoundException) {
                try {
                    val codeStr = DBHelp.findItemTypeByID(inventoryPart.typeID)?.code
                    val colorStr = DBHelp.findColorByID(inventoryPart.colorID)?.code
                    var superCode = DBHelp.findPartByID(inventoryPart.itemID)?.code

                    val url = URL("http://img.bricklink.com/" + codeStr + "/" + colorStr + "/" + superCode + ".gif")
                    val connection = url.openConnection()
                    connection.connect()
                    val lengthOfFile = connection.contentLength
                    val isStream = url.openStream()
                    val testDirectory = File("$filesDir/images")
                    if (!testDirectory.exists()) testDirectory.mkdir()
                    val fos = FileOutputStream("$testDirectory/$superCode.gif")
                    if (superCode != null)
                        ourCode = superCode
                    val data = ByteArray(1024)
                    var count = 0
                    var total: Long = 0
                    var progress = 0
                    count = isStream.read(data)
                    while (count != -1) {
                        total += count.toLong()
                        val progress_temp = total.toInt() * 100/ lengthOfFile
                        if (progress_temp % 10 == 0 && progress != progress_temp) {
                            progress = progress_temp
                        }
                        fos.write(data, 0, count)
                        count = isStream.read(data)
                    }
                    DBHelp.close()
                    isStream.close()
                    fos.close()
                } catch (e: MalformedURLException) {
                    DBHelp.close()
                    return "Malformed URL 2"
                } catch (e: FileNotFoundException) {
                    DBHelp.close()
                    return "FileNotFound 2"
                } catch (e: IOException) {
                    DBHelp.close()
                    return "IO Exception 2"
                }
                DBHelp.close()
                return "success2"
            } catch (e: IOException) {
                DBHelp.close()
                return "IO Exception"
            }
            DBHelp.close()
            return "success"
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
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

                val params = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT)
                params.setMargins(0,0,0,10)
                row.layoutParams = params
                row.setBackgroundColor(android.graphics.Color.LTGRAY)

                image.setImageDrawable(resources.getDrawable(R.drawable.ic_launcher_foreground))
                image.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.MATCH_PARENT)
                linLayH1.addView(image)


                val inventoryPart = DBHelp.findInventoryPartByID(cursor.getInt(0))
                if (inventoryPart != null) {
                    val downloader = ImageDownloader(inventoryPart, this, image)
                    downloader.execute()
                }

                val text = TextView(this)
                text.setText(Html.fromHtml("<b>Name</b>: " + itemname + "<br/>"
                        + "<b>Code:</b> [" + itemcode + "]" + "<br/>"
                        + "<b>Amount:</b> "+ quantityHave.toString() + "/" + quanitity.toString(), 0))
                text.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.MATCH_PARENT)
                linLayH1.addView(text)

                val plusBtn = Button(this)
                plusBtn.setTag(cursor.getInt(0))
                plusBtn.setText("+")
                plusBtn.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1.0f)
                plusBtn.setOnClickListener {
                    var inventoryPart = DBHelp.findInventoryPartByID(it.tag as Int)
                    if (inventoryPart != null) {
                        var newCount = inventoryPart.quantityInStore
                        if (inventoryPart.quantityInStore < inventoryPart.quantityInSet) {
                            inventoryPart.quantityInStore += 1
                            newCount = inventoryPart.quantityInStore
                            DBHelp.editInventoryPart(inventoryPart)
                        }
                        text.setText(Html.fromHtml("<b>Name</b>: " + itemname + "<br/>"
                                + "<b>Code:</b> [" + itemcode + "]" + "<br/>"
                                + "<b>Amount:</b> "+ newCount + "/" + quanitity.toString(), 0))
                    }
                }

                val minusBtn = Button(this)
                minusBtn.setText("-")
                minusBtn.setTag(cursor.getInt(0))
                minusBtn.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1.0f)
                minusBtn.setOnClickListener {
                    var inventoryPart = DBHelp.findInventoryPartByID(it.tag as Int)
                    if (inventoryPart != null) {
                        var newCount = inventoryPart.quantityInStore
                        if (inventoryPart.quantityInStore > 0) {
                            inventoryPart.quantityInStore -= 1
                            newCount = inventoryPart.quantityInStore
                            DBHelp.editInventoryPart(inventoryPart)
                        }
                        text.setText(Html.fromHtml("<b>Name</b>: " + itemname + "<br/>"
                                + "<b>Code:</b> [" + itemcode + "]" + "<br/>"
                                + "<b>Amount:</b> "+ newCount + "/" + quanitity.toString(), 0))
                    }
                }
                linLayH2.addView(minusBtn)
                linLayH2.addView(plusBtn)

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

    fun writeXml(inventory: Inventory) {
        val docBuilder: DocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val doc: Document = docBuilder.newDocument()

        val DBHelp = DBHelperino(this)
        val query = "SELECT * FROM inventoriesparts WHERE inventoryid=${inventory.id}"
        val myDB = DBHelp.readableDatabase

        val cursor = myDB.rawQuery(query, null)

        val rootElement: Element = doc.createElement("INVENTORY")

        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            val inventoryPart = InventoryPart(
                cursor.getInt(0),
                cursor.getInt(1),
                cursor.getInt(2),
                cursor.getInt(3),
                cursor.getInt(4),
                cursor.getInt(5),
                cursor.getInt(6),
                cursor.getInt(7)
            )

            if (inventoryPart.quantityInSet == inventoryPart.quantityInStore) {
                cursor.moveToNext()
                continue
            }

            val itemChild: Element = doc.createElement("ITEM")

            val itemTypeChild: Element = doc.createElement("ITEMTYPE")
            val itemTypeCode = DBHelp.findItemTypeByID(inventoryPart.typeID)?.code
            itemTypeChild.appendChild(doc.createTextNode(itemTypeCode))
            itemChild.appendChild(itemTypeChild)

            val itemIDChild: Element = doc.createElement("ITEMID")
            var itemIDCode = DBHelp.findPartByID(inventoryPart.itemID)?.code
            if (itemIDCode != null) {
                itemIDChild.appendChild(doc.createTextNode(itemIDCode))
            }
            else {
                itemIDChild.appendChild(doc.createTextNode("null"))
            }
            itemChild.appendChild(itemIDChild)

            val colorChild: Element = doc.createElement("COLOR")
            val colorCode = DBHelp.findColorByID(inventoryPart.colorID)?.code
            colorChild.appendChild(doc.createTextNode(colorCode.toString()))
            itemChild.appendChild(colorChild)

            val qtyFilledChild: Element = doc.createElement("QTYFILLED")
            val qtyFilledNum = inventoryPart.quantityInSet - inventoryPart.quantityInStore
            qtyFilledChild.appendChild(doc.createTextNode(qtyFilledNum.toString()))
            itemChild.appendChild(qtyFilledChild)

            rootElement.appendChild(itemChild)

            cursor.moveToNext()
        }

        doc.appendChild(rootElement)

        val transformer: Transformer = TransformerFactory.newInstance().newTransformer()
        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")


        //val path = this.filesDir
        //val outDir = File(path, "Output")
        val outDir = getExternalFilesDir("Output")
        outDir?.mkdirs()

        val file = File(outDir, inventory.name + "_"+ inventory.id + ".xml")

        transformer.transform(DOMSource(doc), StreamResult(file))

        toastMessage("XML File saved to ${file.absolutePath}", true)
    }
}
