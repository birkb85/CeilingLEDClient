package dk.birkb85.ceilingledclient.models

import android.os.AsyncTask
import android.util.Log
import dk.birkb85.ceilingledclient.models.Global.Companion.tcpClient

class ConnectTask : AsyncTask<String, String, TcpClient>() {
    override fun doInBackground(vararg message: String): TcpClient? { //we create a TCPClient object
        tcpClient = TcpClient(object : TcpClient.OnMessageReceived {
            //here the messageReceived method is implemented
            override fun messageReceived(message: String?) { //this method calls the onProgressUpdate
                publishProgress(message)
            }
        })
        tcpClient?.run()
        return null
    }

    override fun onProgressUpdate(vararg values: String) {
        super.onProgressUpdate(*values)
        //response received from server
        Log.d("DEBUG", "onProgressUpdate: response: " + values[0])
        //process server response here....
    }
}

//private class Test : AsyncTask<String, String, TcpClient>() {
//    override fun doInBackground(vararg urls: URL): Long {
//        val count = urls.size
//        var totalSize: Long = 0
//        for (i in 0 until count) {
//            totalSize += Downloader.downloadFile(urls[i])
//            publishProgress((i / count.toFloat() * 100).toInt())
//            // Escape early if cancel() is called
//            if (isCancelled) break
//        }
//        return totalSize
//    }
//
//    protected override fun onProgressUpdate(vararg progress: Int) {
//        setProgressPercent(progress[0])
//    }
//
//    override fun onPostExecute(result: Long) {
//        JColorChooser.showDialog("Downloaded $result bytes")
//    }
//}