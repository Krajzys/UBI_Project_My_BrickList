package com.ubiquitous.bricklist

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.text.BoringLayout
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.activity_add_project.*
import kotlinx.android.synthetic.main.content_main.*
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import javax.xml.parsers.DocumentBuilderFactory


class AddProjectActivity : AppCompatActivity() {
    var URLGiven: String? = ""
    var checkedID: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_project)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Add Project"
        addButton.isEnabled = false
        checkButton.isEnabled = true

        URLGiven = PreferenceManager.getDefaultSharedPreferences(this).getString("prefixURL", "")
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

    private inner class XMLDownloader(var projectCode: String?, val check: Boolean = false) : AsyncTask<String, Int, String>() {

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (check) {
                val path = filesDir
                val inDir = File(path, "XML")
                if (inDir.exists()) {
                    Log.w("dir", "dir exists")
                    val file = File(inDir, projectCode + ".xml")
                    addButton.isEnabled = file.exists()
                    if (addButton.isEnabled) {
                        toastMessage("File succesfully downloaded!", true)
                    }
                    else {
                        toastMessage("File couldn't be downloaded!", true)
                    }
                }
                else {
                    toastMessage("File couldn't be downloaded!", true)
                }
            }
            else {
                //loadData(projectCode + ".xml")
            }
        }

        override fun doInBackground(vararg p0: String?): String {
            try {
                val url = URL(URLGiven + projectCode + ".xml")
                val connection = url.openConnection()
                connection.connect()
                val lengthOfFile = connection.contentLength
                val isStream = url.openStream()
                val testDirectory = File("$filesDir/XML")
                if (!testDirectory.exists()) testDirectory.mkdir()
                val fos = FileOutputStream("$testDirectory/$projectCode.xml")
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
                isStream.close()
                fos.close()
            } catch (e: MalformedURLException) {
                return "Malformed URL"
            } catch (e: FileNotFoundException) {
                return "File not found"
            } catch (e: IOException) {
                return "IO Exception"
            }
            return "success"
        }
    }

    fun loadData(fileName: String?, inventoryID: Int) {
        val path = filesDir
        val inDir = File(path, "XML")
        Log.w("file", fileName)

        if (inDir.exists()) {
            Log.w("dir", "dir exists")
            val file = File(inDir, fileName)
            if (file.exists()) {
                Log.w("file", "file exists")
                val xmlDoc: Document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)

                xmlDoc.documentElement.normalize()

                val items: NodeList = xmlDoc.getElementsByTagName("ITEM")

                for (i in 0 until items.length) {
                    val itemNode: Node = items.item(i)
                    Log.w("Item", itemNode.nodeName)

                    if (itemNode.nodeType == Node.ELEMENT_NODE) {
                        val elem = itemNode as Element
                        val children = elem.childNodes

                        val values = ContentValues()
                        val DBHelp = DBHelperino(this)
                        val currIndex = DBHelp.getLastID("inventoriesparts") + 1

                        val inventoryPart = InventoryPart()
                        inventoryPart.id = currIndex
                        inventoryPart.inventoryID = inventoryID
                        inventoryPart.quantityInStore = 0

                        for (j in 0 until children.length) {
                            val node = children.item(j)
                            Log.i("Node Info", node.nodeName + " " + node.textContent)
                            when (node.nodeName) {
                                "ITEMTYPE" -> {
                                    val typeid = DBHelp.findItemType(node.textContent)?.id
                                    if (typeid != null) {
                                        inventoryPart.typeID = typeid
                                    }
                                    else {
                                        inventoryPart.typeID = -1
                                    }
                                }
                                "ITEMID" -> {
                                    val itemid = DBHelp.findPart(node.textContent)?.id
                                    if (itemid != null) {
                                        inventoryPart.itemID = itemid
                                    }
                                }
                                "QTY" -> {
                                    inventoryPart.quantityInSet = node.textContent.toInt()
                                }
                                "COLOR" -> {
                                    val colorid = DBHelp.findColor(node.textContent.toInt())?.id
                                    if (colorid != null) {
                                        inventoryPart.colorID = colorid
                                    }
                                }
                                "EXTRA" -> {
                                    inventoryPart.extra = node.textContent.hashCode()
                                }
                            }
                        }

                        DBHelp.addInventoryPart(inventoryPart)
                    }
                }
            }
        }
    }

    fun toastMessage(content: String, isLong: Boolean) {
        var length = if (isLong) {
            Toast.LENGTH_LONG
        } else {
            Toast.LENGTH_SHORT
        }
        Toast.makeText(this, content, length).show()
    }

    fun checkClick(v: View) {
        checkedID = projectID.text.toString()
        val xmld = XMLDownloader(checkedID, true)
        xmld.execute()
    }

    fun addClick(v: View) {
        val DBHelp = DBHelperino(this)
        val newIndex = DBHelp.getLastID("inventories") + 1
        val nameProj = projectName.text.toString()
        val inventory = Inventory(newIndex, nameProj, 1, 0)

        DBHelp.addInventory(inventory)

        addButton.isEnabled = false
        projectName.setText("")
        projectID.setText("")
        loadData(checkedID + ".xml", newIndex)
    }
}
