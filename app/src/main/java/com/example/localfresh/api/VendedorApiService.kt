package com.example.localfresh.api

import com.example.localfresh.model.vendedor.apartados.MarkReservationCompleteRequest
import com.example.localfresh.model.vendedor.apartados.MarkReservationCompleteResponse
import com.example.localfresh.model.vendedor.apartados.QRVerificationResponse
import com.example.localfresh.model.vendedor.apartados.ReservationDetailSellerResponse
import com.example.localfresh.model.vendedor.apartados.ReservationListSellerResponse
import com.example.localfresh.model.vendedor.notificaciones.MarkNotificationReadRequest
import com.example.localfresh.model.vendedor.notificaciones.MarkNotificationReadResponse
import com.example.localfresh.model.vendedor.notificaciones.SellerNotificationsResponse
import com.example.localfresh.model.vendedor.paginaprincipal.GetSellerInfoResponse
import com.example.localfresh.model.vendedor.paginaprincipal.UpdateSellerInfoResponse
import com.example.localfresh.model.vendedor.producto.AddProductResponse
import com.example.localfresh.model.vendedor.producto.DeleteProductResponse
import com.example.localfresh.model.vendedor.producto.EditProductResponse
import com.example.localfresh.model.vendedor.producto.GetProductResponse
import com.example.localfresh.model.vendedor.producto.GetSellerProductsResponse
import com.example.localfresh.model.vendedor.signup.ResendVerificationVendedorRequest
import com.example.localfresh.model.vendedor.signup.ResendVerificationVendedorResponse
import com.example.localfresh.model.vendedor.signup.SignUpVendedorRequest
import com.example.localfresh.model.vendedor.signup.SignUpVendedorResponse
import com.example.localfresh.model.vendedor.unidad.AddUnitRequest
import com.example.localfresh.model.vendedor.unidad.AddUnitResponse
import com.example.localfresh.model.vendedor.unidad.DeleteUnitResponse
import com.example.localfresh.model.vendedor.unidad.EditUnitRequest
import com.example.localfresh.model.vendedor.unidad.EditUnitResponse
import com.example.localfresh.model.vendedor.unidad.GetUnitResponse
import com.example.localfresh.model.vendedor.unidad.GetUnitsProductSellerResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface VendedorApiService {

    // Endpoint para el registro del vendedor
    @POST("vendedor/registro/registrovendedores.php")
    fun registrarVendedor(@Body datos: SignUpVendedorRequest): Call<SignUpVendedorResponse>

    // Endpoint para reenviar el correo de verificación
    @POST("vendedor/registro/vendedor_resend_verification.php")
    fun reenviarCorreoVerificacion(@Body request: ResendVerificationVendedorRequest): Call<ResendVerificationVendedorResponse>

    // Endpoint para obtener la información del vendedor
    @GET("vendedor/paginaprincipal/getSellerInfo.php")
    fun getSellerInfo(@Query("seller_id") sellerId: Int): Call<GetSellerInfoResponse> // Usar @Query para pasar el ID

    // Endpoint para actualizar la información del vendedor
    @POST("vendedor/paginaprincipal/updateSellerInfo.php")
    fun updateSellerInfoWithImage(@Body requestBody: RequestBody): Call<UpdateSellerInfoResponse>

    // Endpoint para agregar un producto
    @POST("vendedor/paginaprincipal/add_product.php")
    fun addProduct(
        @Body requestBody: MultipartBody
    ): Call<AddProductResponse>

    // Endpoint para obtener los productos del vendedor
    @GET("vendedor/paginaprincipal/getSellerProducts.php")
    fun getSellerProducts(@Query("seller_id") sellerId: Int): Call<GetSellerProductsResponse>

    // Endpoint para editar un producto
    @POST("vendedor/paginaprincipal/edit_product.php")
    fun editProduct(@Body requestBody: MultipartBody): Call<EditProductResponse>

    // Endpoint para obtener la información de un producto
    @GET("vendedor/paginaprincipal/get_product.php")
    fun getProduct(@Query("product_id") productId: Int): Call<GetProductResponse>

    // Endpoint para eliminar un producto
    @HTTP(method = "DELETE", path = "vendedor/paginaprincipal/delete_product.php", hasBody = true)
    fun deleteProduct(@Body requestBody: RequestBody): Call<DeleteProductResponse>

    // Endpoint para agregar una unidad de producto
    @POST("vendedor/paginaprincipal/add_unit.php")
    fun addUnit(@Body request: AddUnitRequest): Call<AddUnitResponse>

    // Endpoint para obtener las unidades de un producto y la información del producto
    @GET("vendedor/paginaprincipal/get_units_product.php")
    fun getUnitsProduct(@Query("product_id") productId: Int): Call<GetUnitsProductSellerResponse>

    // Endpoint para editar una unidad
    @PUT("vendedor/paginaprincipal/edit_unit.php")
    fun editUnit(@Body request: EditUnitRequest): Call<EditUnitResponse>

    // Endpoint para obtener los datos de una unidad
    @GET("vendedor/paginaprincipal/get_unit.php")
    fun getUnit(@Query("unit_id") unitId: Int): Call<GetUnitResponse>

    // Endpoint para eliminar una unidad
    @DELETE("vendedor/paginaprincipal/delete_unit.php")
    fun deleteUnit(@Query("unit_id") unitId: Int): Call<DeleteUnitResponse>

    // Endpoint para obtener los apartados del vendedor
    @GET("vendedor/apartados/get_seller_reservations.php")
    suspend fun getSellerReservations(
        @Query("seller_id") sellerId: Int,
        @Query("status") status: String? = null,
        @Query("date_from") dateFrom: String? = null,
        @Query("date_to") dateTo: String? = null,
        @Query("buyer_search") buyerSearch: String? = null,
        @Query("history") history: String? = null
    ): Response<ReservationListSellerResponse>

    // Endpoint para obtener los detalles de un apartado del vendedor
    @GET("vendedor/apartados/get_reservation_details_seller.php")
    suspend fun getSellerReservationDetails(
        @Query("reservation_id") reservationId: Int,
        @Query("seller_id") sellerId: Int
    ): Response<ReservationDetailSellerResponse>

    // Endpoint para verificar un código QR
    @GET("vendedor/apartados/verify_reservation_qr.php")
    suspend fun verifyReservationQR(
        @Query("qr_code") qrCode: String,
        @Query("seller_id") sellerId: Int
    ): Response<QRVerificationResponse>

    // Endpoint para marcar un apartado como completado
    @POST("vendedor/apartados/mark_reservation_complete.php")
    suspend fun markReservationComplete(
        @Body request: MarkReservationCompleteRequest
    ): Response<MarkReservationCompleteResponse>

    // Endpoint para obtener las notificaciones del vendedor
    @GET("vendedor/notificaciones/get_notifications.php")
    suspend fun getSellerNotifications(
        @Query("seller_id") sellerId: Int,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("type") type: String? = null,
        @Query("is_read") isRead: Int? = null,
        @Query("sort_by") sortBy: String = "created_at",
        @Query("sort_direction") sortDirection: String = "DESC"
    ): Response<SellerNotificationsResponse>

    // Endpoint para marcar una notificación como leída
    @POST("vendedor/notificaciones/mark_notification_read.php")
    fun markNotificationAsRead(@Body request: MarkNotificationReadRequest): Call<MarkNotificationReadResponse>
}