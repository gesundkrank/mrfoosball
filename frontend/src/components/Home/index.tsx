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
import Button from 'react-bootstrap/Button';
import Col from 'react-bootstrap/Col';
import Container from 'react-bootstrap/Container';
import Row from 'react-bootstrap/Row';
import {Redirect} from "react-router";
import Cookies from "universal-cookie/cjs";
import ChannelIdModal from "./components/ChannelIdModal";

interface IHomeState {
    showChannelIdModal: boolean;
    channelId: string | null;
}

class Home extends React.Component<any, IHomeState> {
    private readonly cookies: Cookies;

    constructor(props: Readonly<any>) {
        super(props);

        this.cookies = new Cookies();

        this.state = {
            channelId: this.cookies.get('id'),
            showChannelIdModal: false
        };

        this.handleShowChannelIdModal = this.handleShowChannelIdModal.bind(this);
        this.handleHideChannelIdModal = this.handleHideChannelIdModal.bind(this);

        this.openChannel = this.openChannel.bind(this);
    }

    public handleShowChannelIdModal() {
        this.setState({showChannelIdModal: true});
    }

    public handleHideChannelIdModal() {
        this.setState({showChannelIdModal: false});
    }

    public render() {
        return this.state.channelId ?
            <Redirect to={this.state.channelId}/> :
            <>
                <ChannelIdModal show={this.state.showChannelIdModal} handleClose={this.handleHideChannelIdModal}
                                openChannel={this.openChannel}/>
                <Container>
                    <Row>
                        <Col>
                            <Button onClick={this.handleShowChannelIdModal}>Enter Channel Id</Button>
                            <Button>Scan QR Code</Button>
                        </Col>
                    </Row>
                </Container>
            </>
    }

    private openChannel(channel: string): void {
        this.cookies.set('id', channel);
        this.setState({
            channelId: channel
        })
    }
}

export default Home;
