package com.example.localfresh.api

import com.example.localfresh.model.comprador.PreferenciasCompradorRequest
import com.example.localfresh.model.comprador.ResendVerificationRequest
import com.example.localfresh.model.comprador.SignUpCompradorRequest
import com.example.localfresh.model.comprador.SignUpCompradorResponse
import com.example.localfresh.model.comprador.ResendVerificationResponse
import com.example.localfresh.model.comprador.apartados.AddToCartRequest
import com.example.localfresh.model.comprador.apartados.AddToCartResponse
import com.example.localfresh.model.comprador.apartados.CancelReservationRequest
import com.example.localfresh.model.comprador.apartados.CancelReservationResponse
import com.example.localfresh.model.comprador.apartados.CartResponse
import com.example.localfresh.model.comprador.detalle.DetalleUnidadResponse
import com.example.localfresh.model.comprador.favoritos.FavoriteRequest
import com.example.localfresh.model.comprador.favoritos.FavoritesResponse
import com.example.localfresh.model.comprador.paginaprincipal.PaginaPrincipalCompradorResponse
import com.example.localfresh.model.comprador.paginaprincipal.ProductosResponse
import com.example.localfresh.model.comprador.paginaprincipal.TiendaResponse
import com.example.localfresh.model.comprador.perfil.UserProfileResponse
import com.example.localfresh.model.comprador.apartados.RemoveFromCartRequest
import com.example.localfresh.model.comprador.apartados.RemoveFromCartResponse
import com.example.localfresh.model.comprador.apartados.ReservationDetailResponse
import com.example.localfresh.model.comprador.apartados.ReservationListResponse
import com.example.localfresh.model.comprador.apartados.ReservationRequest
import com.example.localfresh.model.comprador.apartados.ReservationResponse
import com.example.localfresh.model.comprador.estadisticas.EstadisticasResponse
import com.example.localfresh.model.comprador.favoritos.GetFavoritesResponse
import com.example.localfresh.model.comprador.notificaciones.NotificationPreferencesRequest
import com.example.localfresh.model.comprador.notificaciones.NotificationPreferencesResponse
import com.example.localfresh.model.comprador.perfil.UpdateUserProfileRequest
import com.example.localfresh.model.comprador.perfil.UpdateUserProfileResponse
import com.example.localfresh.model.comprador.recomendaciones.RecommendationResponse
import com.example.localfresh.model.comprador.reviews.ReportReviewRequest
import com.example.localfresh.model.comprador.reviews.ReportReviewResponse
import com.example.localfresh.model.comprador.reviews.ReviewRequest
import com.example.localfresh.model.comprador.reviews.ReviewResponse
import com.example.localfresh.model.comprador.reviews.SellerReviewsResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.Query

interface CompradorApiService {

    // Endpoint para el registro del comprador
    @POST("comprador/registro/registrocompradores.php")
    fun registrarComprador(@Body datos: SignUpCompradorRequest): Call<SignUpCompradorResponse>

    // Endpoint para reenviar el correo de verificación
    @POST("comprador/registro/resend_verification.php")
    fun reenviarCorreoVerificacion(@Body request: ResendVerificationRequest): Call<ResendVerificationResponse> // Cambia a ResendVerificationResponse

    // Endpoint para guardar las preferencias del comprador
    @POST("comprador/preferencias/guardar_preferencias.php")
    fun guardarPreferencias(@Body preferencias: PreferenciasCompradorRequest): Call<ResponseBody>

    // Endpoint para obtener las preferencias del comprador
    @GET("comprador/preferencias/get_preferencias.php")
    fun getPreferencias(@Query("user_id") userId: Int): Call<Map<String, Any>>
    
    // Endpoint para obtener las tiendas
    @GET("comprador/paginaprincipal/get_stores.php")
    fun obtenerTiendasYDistancia(
        @Query("store_type") storeType: String? = null,
        @Query("organic") organic: Int? = null,
        @Query("distance") distance: Int? = null
    ): Call<PaginaPrincipalCompradorResponse>
    
    // Endpoint para obtener la información de una tienda
    @GET("comprador/paginaprincipal/get_store_info.php")
    fun obtenerInfoTienda(@Query("seller_id") sellerId: Int): Call<TiendaResponse>

    // Endpoint para obtener los productos de una tienda
    @GET("comprador/paginaprincipal/get_store_products.php")
    fun obtenerProductosTienda(@Query("seller_id") sellerId: Int): Call<ProductosResponse>

    // Endpoint para obtener el detalle de una unidad especifica
    @GET("comprador/paginaprincipal/get_unit_details.php")
    fun obtenerDetalleUnidad(@Query("unit_id") unitId: Int): Call<DetalleUnidadResponse>

    // Endpoint para agregar un producto a favoritos
    @POST("comprador/favoritos/favorites.php")
    fun addToFavorites(@Body request: FavoriteRequest): Call<FavoritesResponse>

    // Endpoint para eliminar un producto de favoritos
    @HTTP(method = "DELETE", path = "comprador/favoritos/favorites.php", hasBody = true)
    fun removeFromFavorites(@Body request: FavoriteRequest): Call<FavoritesResponse>

    // Endpoint para verificar si un producto o tienda es favorito
    @GET("comprador/favoritos/favorites.php")
    fun checkIsFavorite(
        @Query("user_id") userId: Int,
        @Query("seller_id") sellerId: Int? = null,
        @Query("product_id") productId: Int? = null
    ): Call<FavoritesResponse>

    // Endpoint para obtener la lista de favoritos
    @GET("comprador/favoritos/get_favorites.php")
    fun getFavorites(
        @Query("user_id") userId: Int,
        @Query("type") type: String? = null
    ): Call<GetFavoritesResponse>

    // Endpoint para agregar un producto al carrito
    @POST("comprador/apartados/add_to_cart.php")
    fun addToCart(@Body request: AddToCartRequest): Call<AddToCartResponse>

    // Endpoint para obtener el carrito de un usuario
    @GET("comprador/apartados/get_cart.php")
    fun getCart(
        @Query("user_id") userId: Int,
        @Query("cart_id") cartId: Int? = null
    ): Call<CartResponse>

    // Endpoint para eliminar un producto del carrito
    @HTTP(method = "DELETE", path = "comprador/apartados/remove_from_cart.php", hasBody = true)
    fun removeFromCart(
        @Body request: RemoveFromCartRequest
    ): Call<RemoveFromCartResponse>

    // Endpoint para confirmar un apartado
    @POST("comprador/apartados/confirm_reservation.php")
    fun confirmReservation(@Body request: ReservationRequest): Call<ReservationResponse>

    // Endpoint para obtener la lista de apartados
    @GET("comprador/apartados/get_reservations.php")
    fun getReservations(
        @Query("user_id") userId: Int,
        @Query("status") status: String? = null,
        @Query("sort_by") sortBy: String = "reservation_date",
        @Query("sort_direction") sortDirection: String = "DESC",
        @Query("history") history: String? = null
    ): Call<ReservationListResponse>

    // Endpoint para obtener los detalles de un apartado
    @GET("comprador/apartados/get_reservation_details.php")
    fun getReservationDetails(
        @Query("reservation_id") reservationId: Int
    ): Call<ReservationDetailResponse>

    // Endpoint para cancelar un apartado
    @POST("comprador/apartados/cancel_reservation.php")
    fun cancelReservation(@Body request: CancelReservationRequest): Call<CancelReservationResponse>

    // Endpoint para obtener la información del usuario
    @GET("comprador/perfil/get_user_info.php")
    fun getUserProfile(@Query("user_id") userId: Int): Call<UserProfileResponse>

    // Endpoint para actualizar la información del perfil del usuario
    @POST("comprador/perfil/update_user_info.php")
    fun updateUserProfile(@Body request: UpdateUserProfileRequest): Call<UpdateUserProfileResponse>

    // Endpoint para crear una reseña
    @POST("comprador/reviews/submit_review.php")
    fun submitReview(@Body reviewRequest: ReviewRequest): Call<ReviewResponse>
    
    // Endpoint para obtener las reseñas de un vendedor
    @GET("comprador/reviews/get_seller_reviews.php")
    fun getSellerReviews(
        @Query("seller_id") sellerId: Int,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 5
    ): Call<SellerReviewsResponse>

    // Endpoint para reportar una reseña
    @POST("comprador/reviews/report_review.php")
    fun reportReview(@Body request: ReportReviewRequest): Call<ReportReviewResponse>

    // Endpoint para obtener estadísticas del comprador
    @GET("comprador/estadisticas/get_statistics.php")
    fun getStatistics(
        @Query("user_id") userId: Int,
        @Query("period") period: String = "month",
        @Query("group_by") groupBy: String = "category"
    ): Call<EstadisticasResponse>

    // Endpoint para obtener los productos recomendados
    @GET("comprador/paginaprincipal/get_recommended_products.php")
    fun getRecommendations(@Query("user_id") userId: Int): Call<RecommendationResponse>

    // Endpoint para actualizar las preferencias de notificaciones
    @GET("comprador/notificaciones/notification_preferences.php")
    fun getNotificationPreferences(@Query("user_id") userId: Int): Call<NotificationPreferencesResponse>

    // Endpoint para actualizar las preferencias de notificaciones
    @POST("comprador/notificaciones/notification_preferences.php")
    fun updateNotificationPreferences(@Body request: NotificationPreferencesRequest): Call<NotificationPreferencesResponse>
}