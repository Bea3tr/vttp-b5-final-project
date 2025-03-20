import { Injectable } from "@angular/core";
import { ComponentStore } from "@ngrx/component-store";
import { Load, LoadSlice } from "./models/models";

const INIT_LOAD: Load = {
    type: '',
    breed: '',
    size: '',
    gender: '',
    age: '',
    name: '',
    location: '',
    pf_ids: []
}

const INIT_STATE: LoadSlice = {
    load: INIT_LOAD,
    loaded_ids: []
}

@Injectable()
export class ApiStore extends ComponentStore<LoadSlice> {
    constructor() {
        super(INIT_STATE);
    }

    readonly loadForm = this.updater<Load>(
        (store: LoadSlice, load: Load) => {
            console.info('[Store] Updating load')
            return {
                load: load,
                loaded_ids: [ ...store.loaded_ids, ...load.pf_ids ]
            } as LoadSlice
        }
    )

    readonly loadFormIds = this.updater<number[]>(
        (store: LoadSlice, ids: number[]) => {
            return {
                load: store.load,
                loaded_ids: ids
            } as LoadSlice
        }
    )

    readonly getLoaded = this.select<Load>(
        (store: LoadSlice) => {
            // updates the load parameters
            let load = store.load
            return {
                type: load.type,
                breed: load.breed,
                size: load.size,
                gender: load.gender,
                age: load.age,
                name: load.name,
                location: load.location,
                pf_ids: store.loaded_ids
            } 
        }
    )
}