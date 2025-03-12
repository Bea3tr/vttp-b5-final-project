export interface UserInfo {
    name: string,
    email: string,
    picture: string
}

export interface UploadResponse {
    message: string,
    success: boolean,
    postId: string
}

export interface Post {
    id: string,
    user_id: string,
    name: string,
    user_img: string,
    post: string,
    picture: string,
    timestamp: string
}