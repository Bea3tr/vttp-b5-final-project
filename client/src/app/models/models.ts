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
    files: MediaFile[],
    timestamp: string,
    status: string,
    currentFileIndex: number
}

export interface MediaFile {
    id: string,
    type: string,
    file: string
}

export interface PfResult {
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