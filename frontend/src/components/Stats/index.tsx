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
import Col from 'react-bootstrap/Col';
import Container from 'react-bootstrap/Container';
import Row from 'react-bootstrap/Row';
import Tab from 'react-bootstrap/Tab';
import Tabs from 'react-bootstrap/Tabs';
import { Redirect, RouteComponentProps } from 'react-router';
import useGetHttp from '../../hooks/useGetHttp';
import useInterval from '../../hooks/useInterval';
import ITournament from '../../models/ITournament';
import PlayerStats from './components/PlayerStats';

interface IIdParams {
    id: string;
}

export default function Stats(props: RouteComponentProps<IIdParams>) {
    const id = props.match.params.id;
    const [tournament, setTournament] = useState<ITournament | null>(null);
    const counter = useInterval(5000);

    useGetHttp<ITournament>('/api/tournament/' + id + '/running', (promise) => {
        promise.then((response) => setTournament(response.data));
    }, [counter]);

    if (tournament) {
        return <Redirect to={ id + '/match' }/>
    }
    return (
        <Container>
            <Row>
                <Col xs={ 8 }>
                    <Tabs defaultActiveKey="Players" id="stats-tabs">
                        <Tab title="Players">
                            <PlayerStats id={ id }/>
                        </Tab>
                        <Tab title="Teams">
                            TeamStats
                        </Tab>
                    </Tabs>
                </Col>
                <Col xs={ 4 }>Right: { id }</Col>
            </Row>
        </Container>
    );
}
