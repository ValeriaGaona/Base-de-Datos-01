package mx.edu.utng.apptest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var db: AppDatabase
    private lateinit var postDao: PostDao
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = AppDatabase.getInstance(this)
        postDao = db.postDao()
        setContent {
            MaterialTheme {
                PostScreen(postDao)
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostScreen(postDao: PostDao) {
    val scope = rememberCoroutineScope()
    var posts by remember { mutableStateOf<List<PostEntity>>(emptyList()) }
    var text by remember { mutableStateOf("") }
    var editingPost by remember { mutableStateOf<PostEntity?>(null) }

    LaunchedEffect(Unit) {
        posts = postDao.getAll()
    }
    fun refresh() {
        scope.launch { posts = postDao.getAll() }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("BD") }
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        textStyle = TextStyle(color =
                            MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp)
                    )

                }
                Spacer(modifier = Modifier.height(10.dp))
                Button(onClick = {
                    scope.launch {
                        if (editingPost == null) {
                            postDao.insert(PostEntity(content = text))
                        } else {
                            // Update (borrar y reinsertar)
                            postDao.delete(editingPost!!)
                            postDao.insert(PostEntity(content = text))
                            editingPost = null
                        }
                        text = ""
                        refresh()
                    }
                }) {
                    Text(if (editingPost == null) "New" else
                        "Update")
                }
                Spacer(modifier = Modifier.height(35.dp))
                LazyColumn {
                    items(posts) { post ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors =
                                CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement =
                                    Arrangement.SpaceBetween,
                                verticalAlignment =
                                    Alignment.CenterVertically
                            ) {
                                Text(post.content)
                                Row {
                                    TextButton(onClick = {
                                        text = post.content
                                        editingPost = post
                                    }) {
                                        Text("Edit")
                                    }
                                    TextButton(onClick = {
                                        scope.launch {
                                            postDao.delete(post)
                                            refresh()
                                        }
                                    }) {
                                        Text("Delete")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}


@Preview(showBackground = true)
@Composable
fun PostScreenPreview() {
    // Fake DAO para preview: mantiene estado en memoria
    val fakePosts = remember { mutableStateListOf(
        PostEntity(id = 1, content = "Hellows")
    ) }

    // DAO simulado sin funciones suspendidas para evitar errores en preview
    val fakeDao = object : PostDao {
        fun insertSync(post: PostEntity) {
            fakePosts.add(post.copy(id = fakePosts.size + 1))
        }

        fun deleteSync(post: PostEntity) {
            fakePosts.remove(post)
        }


        override suspend fun getAll(): List<PostEntity> {
            TODO("Not yet implemented")
        }

        // Métodos suspendidos requeridos por la interfaz, pero no usados en preview
        override suspend fun insert(post: PostEntity) {
            insertSync(post)
        }

        override suspend fun delete(post: PostEntity) {
            deleteSync(post)
        }

        override suspend fun getById(id: Int): PostEntity {
            TODO("Not yet implemented")
        }

        override suspend fun update(post: PostEntity): Int {
            TODO("Not yet implemented")
        }
    }

    MaterialTheme {
        PostScreen(postDao = fakeDao)
    }
}