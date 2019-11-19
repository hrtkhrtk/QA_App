package jp.techacademy.hirotaka.iwasaki.qa_app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import android.support.design.widget.Snackbar
import android.util.Log
import android.widget.Button

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot

import java.util.HashMap
import java.util.ArrayList
import java.util.HashSet

class QuestionDetailListAdapter(context: Context, private val mQustion: Question) : BaseAdapter(), View.OnClickListener {
    companion object {
        private val TYPE_QUESTION = 0
        private val TYPE_ANSWER = 1
    }

    private var mLayoutInflater: LayoutInflater? = null

    init {
        mLayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getCount(): Int {
        return 1 + mQustion.answers.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            TYPE_QUESTION
        } else {
            TYPE_ANSWER
        }
    }

    override fun getViewTypeCount(): Int {
        return 2
    }

    override fun getItem(position: Int): Any {
        return mQustion
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView

        if (getItemViewType(position) == TYPE_QUESTION) {
            if (convertView == null) {
                convertView = mLayoutInflater!!.inflate(R.layout.list_question_detail, parent, false)!!
            }
            val body = mQustion.body
            val name = mQustion.name

            val bodyTextView = convertView.findViewById<View>(R.id.bodyTextView) as TextView
            bodyTextView.text = body

            val nameTextView = convertView.findViewById<View>(R.id.nameTextView) as TextView
            nameTextView.text = name

        // ↓参考：changeButton.setOnClickListener（など）
            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser

            if (user == null) {
                // ログインしていない場合は何もしない
                //Snackbar.make(v, "ログインしていません", Snackbar.LENGTH_LONG).show()
            } else {
                //val favoriteButton = convertView.findViewById<View>(R.id.favoriteButton) // as Buttonを付けるとエラーになる
                val favoriteButton = convertView.findViewById<Button>(R.id.favoriteButton) // as Buttonを付けるとエラーになる


                val dataBaseReference = FirebaseDatabase.getInstance().reference
                val userRef = dataBaseReference.child(UsersPATH).child(user.uid)
                userRef.addValueEventListener(
                    object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            Log.d("test191106n50", "test191106n50")
                            val userData = snapshot.value as MutableMap<String, String>
                            Log.d("test191106n51", (userData!![FavoritesPATH] == null).toString())
                            if (userData!![FavoritesPATH] == null) {
                                favoriteButton.setBackgroundColor(Color.parseColor("#0000ff")); // 参考：https://seesaawiki.jp/w/moonlight_aska/d/%A5%D3%A5%E5%A1%BC%A4%CE%C7%D8%B7%CA%BF%A7%A4%F2%A4%AB%A4%A8%A4%EB
                                favoriteButton.text = "fav"
                            }
                            else {
                                val existingFavoriteList = userData!![FavoritesPATH] as ArrayList<MutableMap<String, String>>

                                val data = mutableMapOf<String, String>()
                                data.put("genre", mQustion.genre.toString())
                                data.put("questionUid", mQustion.questionUid)

                                //参考：https://engineer-club.jp/java-contains#CollectionListSetQueueStack
                                if (!(existingFavoriteList.contains(data))) { // 含まれなければ
                                    favoriteButton.setBackgroundColor(Color.parseColor("#0000ff")); // 参考：https://seesaawiki.jp/w/moonlight_aska/d/%A5%D3%A5%E5%A1%BC%A4%CE%C7%D8%B7%CA%BF%A7%A4%F2%A4%AB%A4%A8%A4%EB
                                    favoriteButton.text = "fav"
                                }
                                else { // 含まれていれば
                                    favoriteButton.setBackgroundColor(Color.parseColor("#ff0000")); // 参考：https://seesaawiki.jp/w/moonlight_aska/d/%A5%D3%A5%E5%A1%BC%A4%CE%C7%D8%B7%CA%BF%A7%A4%F2%A4%AB%A4%A8%A4%EB
                                    favoriteButton.text = "unfav"
                                }

                            }
                        }
                        override fun onCancelled(firebaseError: DatabaseError) {}
                    }
                )

                favoriteButton.visibility = View.VISIBLE
                favoriteButton.setOnClickListener(this) // 書き方についてLesson4項目3.1参照
            }


            val bytes = mQustion.imageBytes
            if (bytes.isNotEmpty()) {
                val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size).copy(Bitmap.Config.ARGB_8888, true)
                val imageView = convertView.findViewById<View>(R.id.imageView) as ImageView
                imageView.setImageBitmap(image)
            }
        } else {
            if (convertView == null) {
                convertView = mLayoutInflater!!.inflate(R.layout.list_answer, parent, false)!!
            }

            val answer = mQustion.answers[position - 1]
            val body = answer.body
            val name = answer.name

            val bodyTextView = convertView.findViewById<View>(R.id.bodyTextView) as TextView
            bodyTextView.text = body

            val nameTextView = convertView.findViewById<View>(R.id.nameTextView) as TextView
            nameTextView.text = name
        }

        return convertView
    }

    override fun onClick(v: View) {
        // ↓参考：changeButton.setOnClickListener（など）
        // ログイン済みのユーザーを取得する
        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            // ログインしていない場合は何もしない
            Snackbar.make(v, "ログインしていません", Snackbar.LENGTH_LONG).show()
        } else {
            // Firebaseに保存する
            val dataBaseReference = FirebaseDatabase.getInstance().reference
            //val favoriteRef = dataBaseReference.child(UsersPATH).child(user.uid).child(FavoritesPATH)
            val userRef = dataBaseReference.child(UsersPATH).child(user.uid)

            var userData : MutableMap<String, String>? = null
              // ↑Lesson8項目8.5の
              // 「val map = dataSnapshot.value as Map<String, String>」
              // 「val answerMap = map["answers"] as Map<String, String>?」
              //  から類推してこれでいい


            // ↓参考：Lesson8「Firebaseからデータを一度だけ取得する場合はDatabaseReferenceクラスが実装しているQueryクラスのaddListenerForSingleValueEventメソッドを使います。」
              // これも参照→https://firebase.google.com/docs/database/android/retrieve-data?hl=ja
            userRef.addListenerForSingleValueEvent(

                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        userData = snapshot.value as MutableMap<String, String>

                        if (userData!![FavoritesPATH] == null) {
                            val existingFavoriteList = ArrayList<MutableMap<String, String>>()
                            val data = mutableMapOf<String, String>()
                            data.put("genre", mQustion.genre.toString())
                            data.put("questionUid", mQustion.questionUid)
                            existingFavoriteList.add(data)
                            dataBaseReference.child(UsersPATH).child(user.uid).child(FavoritesPATH).setValue(existingFavoriteList)

                        }
                        else {
                            val existingFavoriteList = userData!![FavoritesPATH] as ArrayList<MutableMap<String, String>>
                            val data = mutableMapOf<String, String>()
                            data.put("genre", mQustion.genre.toString())
                            data.put("questionUid", mQustion.questionUid)

                            //参考：https://engineer-club.jp/java-contains#CollectionListSetQueueStack
                            if (!(existingFavoriteList.contains(data))) { // 含まれなければ追加
                                existingFavoriteList.add(data)
                                dataBaseReference.child(UsersPATH).child(user.uid).child(FavoritesPATH).setValue(existingFavoriteList)
                            }
                            else { // 含まれていれば削除
                                existingFavoriteList.remove(data) // 参考：Lesson3項目11.3
                                dataBaseReference.child(UsersPATH).child(user.uid).child(FavoritesPATH).setValue(existingFavoriteList)
                            }

                        }
                    }
                    override fun onCancelled(firebaseError: DatabaseError) {}
                }
            )
        }
    }
}