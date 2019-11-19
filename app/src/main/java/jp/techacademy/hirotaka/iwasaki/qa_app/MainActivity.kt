package jp.techacademy.hirotaka.iwasaki.qa_app

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import android.support.design.widget.Snackbar
import android.util.Base64  //追加する
import android.util.Log
import android.view.View
import android.widget.ListView
import com.google.firebase.database.*

//import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var mToolbar: Toolbar
    private var mGenre = 0

    // --- ここから ---
    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mListView: ListView
    private lateinit var mQuestionArrayList: ArrayList<Question>
    private lateinit var mAdapter: QuestionsListAdapter

    private var mGenreRef: DatabaseReference? = null
    private var mUserRef: DatabaseReference? = null

    private val mEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<String, String>
            val title = map["title"] ?: ""
            val body = map["body"] ?: ""
            val name = map["name"] ?: ""
            val uid = map["uid"] ?: ""
            val imageString = map["image"] ?: ""
            val bytes =
                    if (imageString.isNotEmpty()) {
                        Base64.decode(imageString, Base64.DEFAULT)
                    } else {
                        byteArrayOf()
                    }

            val answerArrayList = ArrayList<Answer>()
            val answerMap = map["answers"] as Map<String, String>?
            if (answerMap != null) {
                for (key in answerMap.keys) {
                    val temp = answerMap[key] as Map<String, String>
                    val answerBody = temp["body"] ?: ""
                    val answerName = temp["name"] ?: ""
                    val answerUid = temp["uid"] ?: ""
                    val answer = Answer(answerBody, answerName, answerUid, key)
                    answerArrayList.add(answer)
                }
            }

            val question = Question(title, body, name, uid, dataSnapshot.key ?: "",
                    mGenre, bytes, answerArrayList)
            mQuestionArrayList.add(question)
            mAdapter.notifyDataSetChanged()
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<String, String>

            // 変更があったQuestionを探す
            for (question in mQuestionArrayList) {
                if (dataSnapshot.key.equals(question.questionUid)) {
                    // このアプリで変更がある可能性があるのは回答(Answer)のみ
                    question.answers.clear()
                    val answerMap = map["answers"] as Map<String, String>?
                    if (answerMap != null) {
                        for (key in answerMap.keys) {
                            val temp = answerMap[key] as Map<String, String>
                            val answerBody = temp["body"] ?: ""
                            val answerName = temp["name"] ?: ""
                            val answerUid = temp["uid"] ?: ""
                            val answer = Answer(answerBody, answerName, answerUid, key)
                            question.answers.add(answer)
                        }
                    }

                    mAdapter.notifyDataSetChanged()
                }
            }
        }

        override fun onChildRemoved(p0: DataSnapshot) {

        }

        override fun onChildMoved(p0: DataSnapshot, p1: String?) {

        }

        override fun onCancelled(p0: DatabaseError) {

        }
    }
    // --- ここまで追加する ---

    private val mEventListenerForFavorite = object : ValueEventListener {
        // TODO:

        override fun onDataChange(dataSnapshot: DataSnapshot) {
            Log.d("test191107n002", "test191107n002")
            val map = dataSnapshot.value as Map<String, String> // Map<String, String>? としなくていい？
            val favoriteList = map["favorites"] as java.util.ArrayList<MutableMap<String, String>> // 参考：C:\Users\USER\Documents\TechAcademy Android\QA_App\app\src\main\java\jp\techacademy\hirotaka\iwasaki\qa_app\QuestionDetailListAdapter.kt



            val questionUidList = favoriteList.map { favoriteElement -> favoriteElement["questionUid"] } // 参考：https://teratail.com/questions/144040「Listの要素を別のものに置き換えた新しいListを作るには」
            var index = 0
            var indexListForRemove = ArrayList<Int>()

            //val QuestionArrayListCopy = mQuestionArrayList
            for (q2 in mQuestionArrayList) {
            //for (q2 in QuestionArrayListCopy) {
                if (!(questionUidList.contains(q2.questionUid))) { // 含まれていなければ
                    //val questionRef = FirebaseDatabase.getInstance().reference.child(ContentsPATH).child(q2.genre).child(q2.questionUid)
                    val questionRef = FirebaseDatabase.getInstance().reference.child(ContentsPATH).child(q2.genre.toString()).child(q2.questionUid)
                    //questionRef.removeEventListener(localEventListener)

/*
                    val localEventListener =
                        object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                // この中は「val mEventListener」の「fun onChildAdded」と同じでいいと思う
                                //val map2 = snapshot.value as Map<String, String> // <String, String> でいい？
                                val map2 = snapshot.value as Map<String, String>? // 質問が削除されたらnullになりそうだからnull許容型にする
                                if (map2 != null) { // smart cast
                                    //val map2 = dataSnapshot.value as Map<String, String>
                                    val title = map2["title"] ?: ""
                                    val body = map2["body"] ?: ""
                                    val name = map2["name"] ?: ""
                                    val uid = map2["uid"] ?: ""
                                    val imageString = map2["image"] ?: ""
                                    val bytes =
                                            if (imageString.isNotEmpty()) {
                                                Base64.decode(imageString, Base64.DEFAULT)
                                            } else {
                                                byteArrayOf()
                                            }

                                    val answerArrayList = ArrayList<Answer>()
                                    val answerMap = map2["answers"] as Map<String, String>?
                                    if (answerMap != null) {
                                        for (key in answerMap.keys) {
                                            val temp = answerMap[key] as Map<String, String>
                                            val answerBody = temp["body"] ?: ""
                                            val answerName = temp["name"] ?: ""
                                            val answerUid = temp["uid"] ?: ""
                                            val answer = Answer(answerBody, answerName, answerUid, key)
                                            answerArrayList.add(answer)
                                        }
                                    }


                                    val question = Question(title, body, name, uid, q2.questionUid, q2.genre, bytes, answerArrayList)

                                    var targetForRemove: Question? = null
                                    for (q in mQuestionArrayList) { // 参考：Lesson8項目8.5
                                        if (q2.questionUid.equals(q.questionUid)) {
                                            targetForRemove = q
                                        }
                                    }

                                    if (targetForRemove != null) {
                                        mQuestionArrayList.remove(targetForRemove)
                                    }
                                    mQuestionArrayList.add(question)



                                    mAdapter.notifyDataSetChanged()
                                }
                            }

                            override fun onCancelled(firebaseError: DatabaseError) {}
                        }


                    questionRef.removeEventListener(localEventListener) // これで機能している？
*/

                    //mQuestionArrayList.remove(q2)

                    indexListForRemove.add(index)
                    Log.d("test191119n01", index.toString())

//                    mAdapter.notifyDataSetChanged() // ここでいい？
                }

                index += 1
            }

            for (index in indexListForRemove) {
                mQuestionArrayList.removeAt(index) // 参考：Lesson3項目11.3
            }






            for (favoriteElement in favoriteList) {
                val genre = favoriteElement["genre"]
                val questionUid = favoriteElement["questionUid"]

                //val questionRef = mDataBaseReference.child(ContentsPATH).child(genre).child(questionUid) // mDataBaseReferenceはまだ使えないっぽい
                val questionRef = FirebaseDatabase.getInstance().reference.child(ContentsPATH).child(genre!!).child(questionUid!!)
                //questionRef.addListenerForSingleValueEvent( // addListenerForSingleValueEvent or addValueEventListener // 参考：https://firebase.google.com/docs/database/android/retrieve-data?hl=ja



                val localEventListener =
                    object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            // この中は「val mEventListener」の「fun onChildAdded」と同じでいいと思う
                            //val map2 = snapshot.value as Map<String, String> // <String, String> でいい？
                            val map2 = snapshot.value as Map<String, String>? // 質問が削除されたらnullになりそうだからnull許容型にする
                            if (map2 != null) { // smart cast
                                //val map2 = dataSnapshot.value as Map<String, String>
                                val title = map2["title"] ?: ""
                                val body = map2["body"] ?: ""
                                val name = map2["name"] ?: ""
                                val uid = map2["uid"] ?: ""
                                val imageString = map2["image"] ?: ""
                                val bytes =
                                        if (imageString.isNotEmpty()) {
                                            Base64.decode(imageString, Base64.DEFAULT)
                                        } else {
                                            byteArrayOf()
                                        }

                                val answerArrayList = ArrayList<Answer>()
                                val answerMap = map2["answers"] as Map<String, String>?
                                if (answerMap != null) {
                                    for (key in answerMap.keys) {
                                        val temp = answerMap[key] as Map<String, String>
                                        val answerBody = temp["body"] ?: ""
                                        val answerName = temp["name"] ?: ""
                                        val answerUid = temp["uid"] ?: ""
                                        val answer = Answer(answerBody, answerName, answerUid, key)
                                        answerArrayList.add(answer)
                                    }
                                }


                                val question = Question(title, body, name, uid, questionUid, genre!!.toInt(), bytes, answerArrayList)

                                var targetForRemove: Question? = null
                                for (q in mQuestionArrayList) { // 参考：Lesson8項目8.5
                                    if (questionUid.equals(q.questionUid)) {
                                        targetForRemove = q
                                    }
                                }

                                if (targetForRemove != null) {
                                    //mQuestionArrayList.remove(targetForRemove)
                                    mQuestionArrayList.remove(targetForRemove!!)
                                }
                                mQuestionArrayList.add(question)



                                mAdapter.notifyDataSetChanged()
                            }
                        }

                        override fun onCancelled(firebaseError: DatabaseError) {}
                    }


                questionRef.removeEventListener(localEventListener)
                questionRef.addValueEventListener( // addListenerForSingleValueEvent or addValueEventListener // 参考：https://firebase.google.com/docs/database/android/retrieve-data?hl=ja
//                questionRef.addListenerForSingleValueEvent( // addListenerForSingleValueEvent or addValueEventListener // 参考：https://firebase.google.com/docs/database/android/retrieve-data?hl=ja
                    // addListenerForSingleValueEventだと
                    // https://gyazo.com/a3235617d00de67fe2bb44a3aef48472（取得済み） の
                    // https://gyazo.com/395bf5c11d1672334c9f3ead2975c12d（取得済み） の数字が更新されない
                    // イベントリスナをremoveする必要がありそうな気がする
                    localEventListener
                )

            }
        }

        override fun onCancelled(firebaseError: DatabaseError) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(mToolbar)

        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener { view ->
            // ジャンルを選択していない場合（mGenre == 0）はエラーを表示するだけ
            if (mGenre == 0) {
                Snackbar.make(view, "ジャンルを選択して下さい", Snackbar.LENGTH_LONG).show()
            } else {
                // ログイン済みのユーザーを取得する
                val user = FirebaseAuth.getInstance().currentUser

                if (user == null) {
                    // ログインしていなければログイン画面に遷移させる
                    val intent = Intent(applicationContext, LoginActivity::class.java)
                    startActivity(intent)
                } else {
                    // ジャンルを渡して質問作成画面を起動する
                    val intent = Intent(applicationContext, QuestionSendActivity::class.java)
                    intent.putExtra("genre", mGenre)
                    startActivity(intent)
                }
            }
            /*
            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser

            if (user == null) {
                // ログインしていなければログイン画面に遷移させる
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            } else {
                // ジャンルを渡して質問作成画面を起動する
                val intent = Intent(applicationContext, QuestionSendActivity::class.java)
                intent.putExtra("genre", mGenre)
                startActivity(intent)
            }
            */
        }
        // --- ここまで修正 ---

        // ナビゲーションドロワーの設定
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(this, drawer, mToolbar, R.string.app_name, R.string.app_name)
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        //課題のために追加↓
        // 参考↓：https://qiita.com/araiyusuke/items/9ce5f2abb8c574f349d1 「ナビゲーションのヘッダーやメニューにアクセスする」
        //       ：http://blog.techfirm.co.jp/2016/02/15/design-support-library-navigationview%E3%81%AB%E3%81%A4%E3%81%84%E3%81%A6/
                    //　なお、「import android.support.design.widget.NavigationView;」は元からimportしていた
        // ログイン済みのユーザーを取得する
        // removeAuthStateListenerしなくていい？（参考：https://firebase.google.com/docs/reference/android/com/google/firebase/auth/FirebaseAuth.AuthStateListener）
        FirebaseAuth.getInstance().addAuthStateListener { // 参考：https://code.luasoftware.com/tutorials/android/firebase-authentication-on-android/
                                                          // 参考：https://firebase.google.com/docs/reference/android/com/google/firebase/auth/FirebaseAuth.AuthStateListener
            //Log.d("test191107n001", "addAuthStateListener") // test成功
            val user = FirebaseAuth.getInstance().currentUser
            val menuNav = navigationView.getMenu()
            val nav_favorite = menuNav.findItem(R.id.nav_favorite)
            if (user == null) {
                // ログインしていなければ非表示にする
                //nav_favorite.visibility = View.VISIBLE // これは動作しない
                //nav_favorite.setTitle("ほげ") // これは動作した
                nav_favorite.setVisible(false) // これは動作した // 参考：https://stackoverflow.com/questions/10692755/how-do-i-hide-a-menu-item-in-the-actionbar
            }
            else {
                //ログインしていれば表示する
                nav_favorite.setVisible(true) // 参考：https://stackoverflow.com/questions/10692755/how-do-i-hide-a-menu-item-in-the-actionbar
            }
        }
        //課題のために追加↑

        // --- ここから ---
        // Firebase
        mDatabaseReference = FirebaseDatabase.getInstance().reference

        // ListViewの準備
        mListView = findViewById(R.id.listView)
        mAdapter = QuestionsListAdapter(this)
        mQuestionArrayList = ArrayList<Question>()
        mAdapter.notifyDataSetChanged()
        // --- ここまで追加する ---

        mListView.setOnItemClickListener { parent, view, position, id ->
            // Questionのインスタンスを渡して質問詳細画面を起動する
            val intent = Intent(applicationContext, QuestionDetailActivity::class.java)
            intent.putExtra("question", mQuestionArrayList[position])
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        val navigationView = findViewById<NavigationView>(R.id.nav_view)

        // 1:趣味を既定の選択とする
        if(mGenre == 0) {
            onNavigationItemSelected(navigationView.menu.getItem(0))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.action_settings) {
            val intent = Intent(applicationContext, SettingActivity::class.java)
            startActivity(intent)
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        // ログイン済みのユーザーを取得する
        val user = FirebaseAuth.getInstance().currentUser

        if (id == R.id.nav_hobby) {
            mToolbar.title = "趣味"
            mGenre = 1
        } else if (id == R.id.nav_life) {
            mToolbar.title = "生活"
            mGenre = 2
        } else if (id == R.id.nav_health) {
            mToolbar.title = "健康"
            mGenre = 3
        } else if (id == R.id.nav_compter) {
            mToolbar.title = "コンピューター"
            mGenre = 4
        } else if (id == R.id.nav_favorite && user != null) {
            mToolbar.title = "お気に入り"
            mGenre = -1
        }

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)

        // --- ここから ---
        // 質問のリストをクリアしてから再度Adapterにセットし、AdapterをListViewにセットし直す
        mQuestionArrayList.clear()
        mAdapter.setQuestionArrayList(mQuestionArrayList)
        mListView.adapter = mAdapter

        // 選択したジャンルにリスナーを登録する
        if (mGenreRef != null) {
            mGenreRef!!.removeEventListener(mEventListener)
        }
        if (mUserRef != null) { // 課題のために追加
            mUserRef!!.removeEventListener(mEventListenerForFavorite)
        }

        if (mGenre > 0) {
            mGenreRef = mDatabaseReference.child(ContentsPATH).child(mGenre.toString())
            mGenreRef!!.addChildEventListener(mEventListener)
        } else if (mGenre < 0 && user != null) {
            mUserRef = mDatabaseReference.child(UsersPATH).child(user.uid) // userはsmart castされている（Lesson3）
            //mUserRef!!.addChildEventListener(mEventListenerForFavorite)
            mUserRef!!.addValueEventListener(mEventListenerForFavorite)
        }

        // --- ここまで追加する ---

        return true
    }
}
