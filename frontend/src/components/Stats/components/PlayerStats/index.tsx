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
import { useState } from 'react';
import Table from 'react-bootstrap/Table';
import useGetHttp from '../../../../hooks/useGetHttp';
import useInterval from '../../../../hooks/useInterval';

interface IIdProps {
    id: string;
}

interface IPlayerStats {
    id: string;
    name: string;
    avatarImage: string;
    games: string;
    wins: string;
    skill: string;
}

function PlayerStats(props: IIdProps) {
    const counter = useInterval(5000);
    const [data, setData] = useState<IPlayerStats[]>([]);

    useGetHttp<IPlayerStats[]>('/api/stats/' + props.id, (promise) => {
        promise.then((response) => {
                setData(response.data)
            })
    }, [counter]);

    return <Table>
        <thead>
        <tr>
            <th>Rank</th>
            <th>Player</th>
            <th/>
            <th>Skill</th>
            <th>Win Rate</th>
        </tr>
        </thead>
        <tbody>
        { data.map((player, i) => {
            return (
                <tr key={ player.id }>
                    <td>{ i + 1 }</td>
                    <td><img src={ player.avatarImage }/></td>
                    <td>{ player.name }</td>
                    <td>{ player.skill }</td>
                </tr>
            );
        }) }
        </tbody>
    </Table>;
}

export default PlayerStats;
