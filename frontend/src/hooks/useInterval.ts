/*
 * This file is part of kicker (https://github.com/mbrtargeting/kicker).
 * Copyright (c) 2019 Jan Graßegger.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import * as React from 'react';
import { useEffect, useRef, useState } from 'react';

export default function useInterval(ms: number): number {
    const [counter, setCounter] = useState(0);
    const r: React.MutableRefObject<any> = useRef(null);
    r.current = { counter, setCounter };

    useEffect(
        () => {
            const id = setInterval(() => {
                r.current.setCounter(r.current.counter + 1)
            }, ms);
            return () => {
                clearInterval(id);
            };
        },
        ['once'],
    );

    return r.current.counter;
}
