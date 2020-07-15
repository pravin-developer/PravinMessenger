package com.pravin.pravinmessenger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_latest_message.*
import kotlinx.android.synthetic.main.latest_message_row.view.*
import kotlinx.android.synthetic.main.user_row_layout.view.*

class LatestMessageActivity : AppCompatActivity() {

    companion object{
        var currentUser: User? = null
        val TAG = "LatestMessages"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_message)

        recyclerview_latestmessages.adapter = adapter
        recyclerview_latestmessages.addItemDecoration(DividerItemDecoration(this,DividerItemDecoration.VERTICAL))


        adapter.setOnItemClickListener{ item, view ->
            Log.d(TAG, "123")
            val intent = Intent(this,ChatActivity::class.java)

            val row = item as LatestMessagesRow


           intent.putExtra(NewMessageActivity.USER_KEY,  row.chatPartnerUser)
            startActivity(intent)

        }


       // setUpDummy()
        listenForLatestMessages()
        fetchCurrentUser()

        verifyUserIsLoggedIn()
    }
    class LatestMessagesRow(val chatMessage: ChatActivity.ChatMessage): Item<ViewHolder>(){
        var chatPartnerUser: User? = null
        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.latestmessage_textview.text = chatMessage.text

            val chatPartnerId: String
            if(chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                chatPartnerId = chatMessage.toId
            }else{
                chatPartnerId = chatMessage.fromId
            }
            val ref = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")
            ref.addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(p0: DataSnapshot) {
                    chatPartnerUser = p0.getValue((User::class.java))
                    viewHolder.itemView.username_textview.text = chatPartnerUser?.username


                    val targetImageView = viewHolder.itemView.latest_imageView

                    Picasso.get().load(chatPartnerUser?.profileImageUrl).into(targetImageView)

                }
                override fun onCancelled(p0: DatabaseError) {

                }
            } )


        }
        override fun getLayout(): Int {
            return R.layout.latest_message_row
        }
    }

    val latestmessagesMap = HashMap<String, ChatActivity.ChatMessage>()

    private fun refreshRecyclerViewMessages(){
        adapter.clear()
        latestmessagesMap.values.forEach {
            adapter.add(LatestMessagesRow(it))
        }
    }

    private fun listenForLatestMessages(){
        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId")
        ref.addChildEventListener(object: ChildEventListener{
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatActivity.ChatMessage::class.java) ?:return

                latestmessagesMap[p0.key!!] = chatMessage
                refreshRecyclerViewMessages()


            }
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatActivity.ChatMessage::class.java) ?:return

                latestmessagesMap[p0.key!!] = chatMessage
                refreshRecyclerViewMessages()


            }
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }
            override fun onChildRemoved(p0: DataSnapshot) {

            }
            override fun onCancelled(p0: DatabaseError) {

            }
        } )
    }
    val adapter = GroupAdapter<ViewHolder>()
   // private fun  setUpDummy(){

     //   adapter.add(LatestMessagesRow())
       // adapter.add(LatestMessagesRow())
        //adapter.add(LatestMessagesRow())

   // }
    private fun fetchCurrentUser(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                currentUser = p0.getValue(User::class.java)
                Log.d("LatestMessages","Current user ${currentUser?.profileImageUrl}")
            }
            override fun onCancelled(p0: DatabaseError) {

            }

        } )
    }

    private fun verifyUserIsLoggedIn() {
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_new_message ->{
                val intent = Intent(this,NewMessageActivity::class.java)
                startActivity(intent)

            }
            R.id.menu_sign_out ->{
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, RegisterActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }
}
