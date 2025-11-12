import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: Data
)

data class Data(
    @SerializedName("userInfo") val userInfo: UserInfo,
    @SerializedName("tokenData") val tokenData: TokenData
)

data class UserInfo(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("image") val image: String,
    @SerializedName("role") val role: String,
    @SerializedName("isVerified") val isVerified: Boolean
)

data class TokenData(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String
)