package com.example.localfresh.repository.vendedor

import com.example.localfresh.model.vendedor.producto.AddProductRequest
import com.example.localfresh.model.vendedor.producto.AddProductResponse
import com.example.localfresh.model.vendedor.producto.DeleteProductResponse
import com.example.localfresh.model.vendedor.producto.EditProductRequest
import com.example.localfresh.model.vendedor.producto.EditProductResponse
import com.example.localfresh.model.vendedor.producto.GetProductResponse
import com.example.localfresh.model.vendedor.producto.GetSellerProductsResponse
import com.example.localfresh.network.RetrofitInstance
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProductVendedorRepository {

    // Metodo para agregar un producto
    fun addProduct(token: String, productRequest: AddProductRequest, onResult: (AddProductResponse?) -> Unit, onError: (String) -> Unit) {
        // Verificar que el token no sea nulo
        if (token.isEmpty()) {
            onError("No se encontró el token de autenticación")
            return
        }

        val builder = MultipartBody.Builder().setType(MultipartBody.FORM)

        // Añadir los campos de texto al builder
        builder.addFormDataPart("seller_id", productRequest.seller_id.toString())
        builder.addFormDataPart("name", productRequest.name)
        builder.addFormDataPart("description", productRequest.description)
        builder.addFormDataPart("category", productRequest.category)
        builder.addFormDataPart("price", productRequest.price.toString())
        builder.addFormDataPart("expiry_type", productRequest.expiry_type) // Añadir expiry_type

        // Manejar la carga de la imagen
        productRequest.image_file?.let { file ->
            val requestFile = file.asRequestBody("image/*".toMediaType())
            builder.addFormDataPart("image_url", file.name, requestFile)
        }

        val requestBody = builder.build()

        // Llamar al servicio API con el token
        RetrofitInstance.vendedorApiService.addProduct(
            requestBody
        ).enqueue(object : Callback<AddProductResponse> {
            override fun onResponse(call: Call<AddProductResponse>, response: Response<AddProductResponse>) {
                if (response.isSuccessful) {
                    onResult(response.body())
                } else {
                    onError("Error al agregar el producto")
                }
            }

            override fun onFailure(call: Call<AddProductResponse>, t: Throwable) {
                onError("Fallo de conexión: ${t.message}")
            }
        })
    }

    // Metodo para obtener productos de un vendedor
    fun getSellerProducts(sellerId: Int, onResult: (GetSellerProductsResponse?) -> Unit, onError: (String) -> Unit) {
        RetrofitInstance.vendedorApiService.getSellerProducts(sellerId).enqueue(object : Callback<GetSellerProductsResponse> {
            override fun onResponse(call: Call<GetSellerProductsResponse>, response: Response<GetSellerProductsResponse>) {
                if (response.isSuccessful) {
                    onResult(response.body())
                } else {
                    onError("Error al obtener productos")
                }
            }

            override fun onFailure(call: Call<GetSellerProductsResponse>, t: Throwable) {
                onError("Fallo de conexión: ${t.message}")
            }
        })
    }

    // Metodo para editar un producto
    fun editProduct(productRequest: EditProductRequest, onResult: (EditProductResponse?) -> Unit, onError: (String) -> Unit) {
        val builder = MultipartBody.Builder().setType(MultipartBody.FORM)

        // Añadir datos de texto
        builder.addFormDataPart("product_id", productRequest.product_id.toString())
        builder.addFormDataPart("seller_id", productRequest.seller_id.toString())
        builder.addFormDataPart("name", productRequest.name)
        builder.addFormDataPart("description", productRequest.description)
        builder.addFormDataPart("category", productRequest.category)
        builder.addFormDataPart("price", productRequest.price.toString())
        builder.addFormDataPart("expiry_type", productRequest.expiry_type) // Añadir expiry_type

        // Añadir la imagen si está disponible
        productRequest.image_file?.let { file ->
            val requestFile = file.asRequestBody("image/*".toMediaType())
            builder.addFormDataPart("image_url", file.name, requestFile)
        }

        val requestBody = builder.build()

        // Llamar a la API para editar el producto
        RetrofitInstance.vendedorApiService.editProduct(requestBody).enqueue(object : Callback<EditProductResponse> {
            override fun onResponse(call: Call<EditProductResponse>, response: Response<EditProductResponse>) {
                if (response.isSuccessful) {
                    onResult(response.body())
                } else {
                    onError("Error al editar el producto")
                }
            }

            override fun onFailure(call: Call<EditProductResponse>, t: Throwable) {
                onError("Fallo de conexión: ${t.message}")
            }
        })
    }

    // Metodo para obtener la información de un producto
    fun getProduct(productId: Int, onResult: (GetProductResponse?) -> Unit, onError: (String) -> Unit) {
        RetrofitInstance.vendedorApiService.getProduct(productId).enqueue(object : Callback<GetProductResponse> {
            override fun onResponse(call: Call<GetProductResponse>, response: Response<GetProductResponse>) {
                if (response.isSuccessful) {
                    onResult(response.body())
                } else {
                    onError("Error al obtener la información del producto")
                }
            }

            override fun onFailure(call: Call<GetProductResponse>, t: Throwable) {
                onError("Fallo de conexión: ${t.message}")
            }
        })
    }

    fun deleteProduct(productId: Int, onResult: (Boolean) -> Unit, onError: (String) -> Unit) {
        val requestBody = createDeleteRequestBody(productId)
        RetrofitInstance.vendedorApiService.deleteProduct(requestBody).enqueue(object : Callback<DeleteProductResponse> {
            override fun onResponse(call: Call<DeleteProductResponse>, response: Response<DeleteProductResponse>) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    onResult(true)
                } else {
                    onError("Error al eliminar el producto: ${response.body()?.message}")
                }
            }

            override fun onFailure(call: Call<DeleteProductResponse>, t: Throwable) {
                onError("Fallo de conexión: ${t.message}")
            }
        })
    }

    private fun createDeleteRequestBody(productId: Int): RequestBody {
        val json = "{\"product_id\": $productId}"
        return json.toRequestBody("application/json; charset=utf-8".toMediaType())
    }
}
