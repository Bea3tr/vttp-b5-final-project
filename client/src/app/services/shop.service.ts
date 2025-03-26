import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { BehaviorSubject, lastValueFrom } from 'rxjs';
import { Item } from '../models/models';

@Injectable()
export class ShopService {
  private http = inject(HttpClient);
  reload = new BehaviorSubject<boolean>(false)

  listItem(form: any, files: FileList, userId: string) {
    console.info('Form values:', form.value);
    const formData = new FormData();
    formData.set('item', form.value['item']);
    formData.set('description', form.value['description']);
    formData.set('price', form.value['price']);
    if (files != undefined) {
      for (let i = 0; i < files.length; i++) {
        formData.append('files', files[i]);
      }
    }
    return lastValueFrom(this.http.post<any>(`/api/shop/${userId}`, formData));
  }

  getItems(userId: string) {
    return lastValueFrom(this.http.get<Item[]>(`/api/shop/get/${userId}`));
  }

  getItemsFiltered(filter: string, userId: string) {
    const params = new HttpParams().append('filter', filter);
    return lastValueFrom(
      this.http.get<Item[]>(`/api/shop/get-filtered/${userId}`, { params })
    );
  }

  getItemById(itemId: string) {
    return lastValueFrom(
      this.http.get<Item>(`/api/shop/get-item/${itemId}`)
    );
  }

  getListedItems(userId: string) {
    return lastValueFrom(
      this.http.get<Item[]>(`/api/shop/get-listed/${userId}`)
    );
  }

  purchaseItem(itemId: string) {
    const body = {
      itemId: itemId,
    };
    return lastValueFrom(this.http.put<any>('/api/shop/purchase', { body }));
  }

  deleteItem(itemId: string) {
    const body = {
      itemId: itemId,
    };
    return lastValueFrom(this.http.delete<any>('/api/shop/delete', { body }));
  }

  saveItem(userId: string, itemId: string) {
    const body = {
      userId: userId,
      itemId: itemId,
    };
    return lastValueFrom(this.http.put<any>('/api/shop/save', { body }));
  }

  removeItem(userId: string, itemId: string) {
    const body = {
      userId: userId,
      itemId: itemId,
    };
    return lastValueFrom(this.http.put<any>('/api/shop/remove', { body }));
  }

  getSaved(userId: string) {
    return lastValueFrom(this.http.get<any>(`/api/shop/get-saved/${userId}`));
  }

  reloadSavedItems(boolean: boolean) {
    this.reload.next(boolean)
  }

  reloadItems(boolean: boolean) {
    this.reload.next(boolean)
  }
}
