package App.tipo.Anime

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tipo.AppAnime.R
import com.tipo.AppAnime.databinding.ActivityMainBinding
import App.tipo.Anime.service.AnimeService
import android.widget.Toast
import com.squareup.picasso.Picasso
import org.imaginativeworld.whynotimagecarousel.CarouselItem
import org.imaginativeworld.whynotimagecarousel.ImageCarousel
import org.imaginativeworld.whynotimagecarousel.OnItemClickListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    val list = mutableListOf<CarouselItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val carousel: ImageCarousel = findViewById(R.id.carousel)
        list.add(CarouselItem("https://cdn.myanimelist.net/images/manga/1/157897.jpg", "Berserk"))
        list.add(CarouselItem("https://cdn.myanimelist.net/images/manga/3/179882.jpg", "JoJo no Kimyou na Bouken Part 7: Steel Ball Run"))
        list.add(CarouselItem("https://cdn.myanimelist.net/images/manga/2/253146.jpg", "One Piece"))
        carousel.addData(list)

        carousel.onItemClickListener = object : OnItemClickListener {
            override fun onClick(position: Int, carouselItem: CarouselItem) {
                Toast.makeText(this@MainActivity, "Anime: ${carouselItem.caption}", Toast.LENGTH_SHORT).show()
            }

            override fun onLongClick(position: Int, dataObject: CarouselItem) {

            }
        }


        binding.apply {
            val animeService = AnimeService.create()
            val call = animeService.getTopAnimes()

            call.enqueue(object : Callback<TopAnime> {

                override fun onResponse(call: Call<TopAnime>, response: Response<TopAnime>) {
                    if (response.body() != null) {
                        val top = response.body()!!.top
                        animeRecyclerView.adapter = AnimeAdapter(this@MainActivity,top)
                        animeRecyclerView.layoutManager = GridLayoutManager(this@MainActivity,3)
                    }
                }

                override fun onFailure(call: Call<TopAnime>, t: Throwable) {

                }

            })

            btnSearch.setOnClickListener {
                val searchedAnime = searchInputEditText.text.toString()
                val callSearchedAnime = animeService.getSearchedAnime(searchedAnime)

                callSearchedAnime.enqueue(object : Callback<SearchedAnime> {

                    override fun onResponse(
                        call: Call<SearchedAnime>,
                        response: Response<SearchedAnime>
                    ) {
                        if (response.body() != null) {
                            val searchedAnimes = response.body()!!.results
                            animeRecyclerView.adapter = AnimeAdapter(this@MainActivity,searchedAnimes)
                            animeRecyclerView.layoutManager = GridLayoutManager(this@MainActivity,3)
                        }else{
                            showError()
                        }
                    }

                    private fun showError() {
                        Toast.makeText(this@MainActivity, "no se ha encontrado su busqueda", Toast.LENGTH_SHORT).show()
                    }

                    override fun onFailure(call: Call<SearchedAnime>, t: Throwable) {

                    }

                })
            }
        }
    }

    class AnimeAdapter(
        private val parentActivity: AppCompatActivity,
        private val animes: List<Result>
    ) : RecyclerView.Adapter<AnimeAdapter.CustomViewHolder>() {

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.anime_item_layout, parent, false)
            return CustomViewHolder(view)
        }

        override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
            val anime = animes[position]
            val view = holder.itemView

            val name = view.findViewById<TextView>(R.id.name)
            val image = view.findViewById<ImageView>(R.id.image)

            name.text = anime.title
            Picasso.get().load(anime.imageUrl).into(image)

            view.setOnClickListener {
                AnimeDetailsBottomSheet(anime).apply {
                   show(parentActivity.supportFragmentManager,"AnimeDetailsBottomSheet")
                }
            }
        }

        override fun getItemCount(): Int {
            return animes.size
        }

    }

}