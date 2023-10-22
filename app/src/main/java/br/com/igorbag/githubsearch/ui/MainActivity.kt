package br.com.igorbag.githubsearch.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.widget.Toast.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import br.com.igorbag.githubsearch.R
import br.com.igorbag.githubsearch.data.GitHubService
import br.com.igorbag.githubsearch.domain.Repository
import br.com.igorbag.githubsearch.ui.adapter.RepositoryAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory



class MainActivity : AppCompatActivity() {

    lateinit var nomeUsuario: EditText
    lateinit var btnConfirmar: Button
    lateinit var listaRepositories: RecyclerView
    lateinit var githubApi: GitHubService
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupView()
        showUserName()
        setupRetrofit()
        setupListeners()

    }

    // Metodo responsavel por realizar o setup da view e recuperar os Ids do layout
    fun setupView() {

        //
        nomeUsuario=findViewById(R.id.et_nome_usuario)
        btnConfirmar=findViewById(R.id.btn_confirmar)
        listaRepositories=findViewById(R.id.rv_lista_repositories)
        sharedPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE)
    }

    //metodo responsavel por configurar os listeners click da tela
    private fun setupListeners() {
        //
        btnConfirmar.setOnClickListener{
            saveUserLocal()
            getAllReposByUserName(this)


        }
    }




    private fun saveUserLocal() {
        val sharedPreferencesEditor = sharedPreferences.edit()
        sharedPreferencesEditor.putString("Name", nomeUsuario.text.toString())
        sharedPreferencesEditor.apply()

    }

    private fun showUserName() {
        val save = sharedPreferences.getString("Name", "github")
        nomeUsuario.setText(save)
     }



    fun setupRetrofit() {
        val retrofit= Retrofit.Builder().baseUrl("https://api.github.com/")
            .addConverterFactory(GsonConverterFactory.create()).build()
        githubApi = retrofit.create(GitHubService::class.java)

    }


    fun getAllReposByUserName(context: Context) {
        githubApi.getAllRepositoriesByUser(nomeUsuario.text.toString()).enqueue(object:
            Callback<List<Repository>> {
            override fun onResponse(
                call: Call<List<Repository>>,
                response: Response<List<Repository>>
            ) {
                if(response.isSuccessful){
                    response.body()?.let {
                        setupAdapter(it)
                    }
                }else{
                    makeText(context, "falha ao receber", LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<List<Repository>>, t: Throwable) {
                makeText(context, "falha", LENGTH_LONG).show()
            }

        })
    }


    fun setupAdapter(list: List<Repository>) {

        val repositoryAdapter=RepositoryAdapter(list)
        listaRepositories.apply{
            adapter=repositoryAdapter
        }
        repositoryAdapter.repoItemLister={
            repository ->  openBrowser(repository.htmlUrl)
        }
        repositoryAdapter.btnShareLister={
            repository -> shareRepositoryLink(repository.htmlUrl)
        }

    }



    fun shareRepositoryLink(urlRepository: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, urlRepository)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }


    fun openBrowser(urlRepository: String) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(urlRepository)
            )
        )

    }

}


