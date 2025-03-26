export interface UserInfo {
    id: string,
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
    files: MediaFile[],
    timestamp: string,
    status: string,
    likes: number,
    currentFileIndex: number
}

export interface MediaFile {
    id: string,
    type: string,
    file: string
}

export interface Comment {
    id: string,
    user_id: string,
    post_id: string,
    comment: string,
    timestamp: string,
    name: string,
    picture: string
}

export interface PfResult {
    id: number,
    url: string,
    name: string,
    species: string,
    breed: string,
    color: string,
    age: string,
    gender: string,
    size: string,
    coat: string,
    description: string,
    email: string,
    phone: string,
    attributes: string[],
    environment: string[],
    tags: string[],
    photos: string[],
    videos: string[],
    address: string,
    currentPhotoIndex: number
}

export interface PfResponse {
    message: string,
    results: PfResult[]
}

export interface LoadSlice {
    load : Load,
    loaded_ids: number[]
}

export interface Load {
    type: string,
    breed: string,
    size: string,
    gender: string,
    age: string,
    name: string,
    location: string,
    pf_ids: number[]
}

export interface ChatMessage {
    id: string,
    senderId: string,
    receiverId: string,
    content: string,
    timestamp: number,
    read: boolean
}

export interface Item {
    id: string,
    user_id: string,
    name: string,
    user_img: string,
    item: string,
    description: string,
    price: number,
    files: MediaFile[],
    timestamp: string,
    purchased: boolean,
    currentFileIndex: number
}

export interface ChatUser {
    user: UserInfo,
    type: string
}

export interface SavedChat {
    id: string,
    type: string
}
